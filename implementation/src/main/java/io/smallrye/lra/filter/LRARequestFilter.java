package io.smallrye.lra.filter;

import io.smallrye.lra.SmallRyeLRAClient;
import io.smallrye.lra.utils.LRAConstants;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.NestedLRA;
import org.eclipse.microprofile.lra.annotation.TimeLimit;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;

@Provider
public class LRARequestFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private SmallRyeLRAClient lraClient;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        LRAContextBuilder lraContextBuilder = new LRAContextBuilder();

        String lraHeader = requestContext.getHeaderString(LRAClient.LRA_HTTP_HEADER);
        URL lraId = lraHeader != null ? new URL(lraHeader) : null;
        lraContextBuilder.lraId(lraId);

        LRA lra = resourceMethod.getAnnotation(LRA.class);

        if (lra == null) {
            processHelperLRAAnnotations(lraId, requestContext);
            return;
        }
        
        boolean nested = resourceMethod.getAnnotation(NestedLRA.class) != null;

        boolean lraContextPresent = lraId != null;
        
        switch (lra.value()) {
            /**
             *  If called outside a LRA context a JAX-RS filter will begin a new
             *  LRA for the duration of the method call and when the call completes
             *  another JAX-RS filter will complete the LRA.
             */
            //<li>REQUIRED if there is an LRA present a new LRA is nested under it
            case REQUIRED:
                if (!lraContextPresent) {
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                } else if (nested) {
                    lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                }
                break;

                    /**
                     *  If called outside a LRA context a JAX-RS filter will begin a new
                     *  LRA for the duration of the method call and when the call completes
                     *  another JAX-RS filter will complete the LRA.
                     *
                     *  If called inside a LRA context a JAX-RS filter will suspend it and
                     *  begin a new LRA for the duration of the method call and when the call
                     *  completes another JAX-RS filter will complete the LRA and resume the
                     *  one that was active on entry to the method.
                     */
                    //<li>REQUIRES_NEW the @NestedLRA annotation is ignored
            case REQUIRES_NEW:
                if (!lraContextPresent) {
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                } else {
                    lraContextBuilder.suspend(lraId);
                    requestContext.getHeaders().remove(LRAClient.LRA_HTTP_HEADER);
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                    requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraId.toExternalForm());
                }
                break;

                    /**
                     *  If called outside a transaction context, the method call will return
                     *  with a 412 Precondition Failed HTTP status code
                     *
                     *  If called inside a transaction context the bean method execution will
                     *  then continue within that context.
                     */
                    //<li>MANDATORY a new LRA is nested under the incoming LRA
            case MANDATORY:
                if (!lraContextPresent) {
                    requestContext.abortWith(Response
                            .status(Response.Status.PRECONDITION_FAILED)
                            .entity(Entity.text("LRA type MANDATORY requires to be called inside of a LRA Context"))
                            .build());
                } else if (nested) {
                    lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                }
                break;

                    /**
                     *  If called outside a LRA context the bean method execution
                     *  must then continue outside a LRA context.
                     *
                     *  If called inside a LRA context the managed bean method execution
                     *  must then continue inside this LRA context.
                     */
                    //<li>SUPPORTS if there is an LRA present a new LRA is nested under otherwise
                    // a new top level LRA is begun
            case SUPPORTS:
                if (nested) {
                    if (lraContextPresent) {
                        lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                    } else {
                        lraId = startNewLRA(lraContextBuilder, requestContext);
                    }
                }
                break;

                    /**
                     *  The bean method is executed without a LRA context. If a context is
                     *  present on entry then it is suspend and then resumed after the
                     *  execution has completed.
                     */
                    //<li>NOT_SUPPORTED nested does not make sense and operations on this resource
                    // *   that contain a LRA context will immediately return with a
                    // *   <code>412 Precondition Failed</code> HTTP status code
            case NOT_SUPPORTED:
                if (lraContextPresent) {
                    lraContextBuilder.suspend(lraId);
                    requestContext.getHeaders().remove(LRAClient.LRA_HTTP_HEADER);
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                }
                break;

                    /**
                     *  If called outside a LRA context the managed bean method execution
                     *  must then continue outside a LRA context.
                     *
                     *  If called inside a LRA context the method is not executed and a
                     *  <code>412 Precondition Failed</code> HTTP status code is returned
                     *  to the caller.
                     */
                    //<li>NEVER nested does not make sense and requests that carry a LRA context
                    // *   will immediately return with a <code>412 Precondition Failed</code> HTTP
                    // *   status code
            case NEVER:
                if (lraContextPresent) {
                    requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED)
                            .entity(Entity.text("LRA type NEVER cannot be called inside of a LRA Context"))
                            .build());
                }
                break;

        }

        if (lra.join() && lraId != null) {
            lraClient.joinLRA(lraId, resourceInfo.getResourceClass(), requestContext.getUriInfo().getBaseUri(), null);
        }

        if (lra.cancelOn().length != 0) {
            lraContextBuilder.cancelOn(lra.cancelOn());
        }

        requestContext.setProperty(LRAContext.CONTEXT_PROPERTY_NAME, lraContextBuilder.build());
    }

    private void processHelperLRAAnnotations(URL lraId, ContainerRequestContext requestContext) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(Leave.class)) {
            lraClient.leaveLRA(lraId, resourceInfo.getResourceClass(), requestContext.getUriInfo().getBaseUri());
        }
    }

    private URL startNewLRA(LRAContextBuilder contextBuilder, ContainerRequestContext requestContext) {
        return startNewLRA(null, contextBuilder, requestContext);
    }

    private URL startNewLRA(URL parentLRA, LRAContextBuilder lraContextBuilder, ContainerRequestContext requestContext) {
        if (parentLRA != null) {
            lraContextBuilder.suspend(parentLRA);
        }
        URL lraId = lraClient.startLRA(parentLRA, resourceInfo.getResourceClass().getName(), getTimeLimit());
        updateContexts(lraId, lraContextBuilder, requestContext);
        return lraId;
    }

    private void updateContexts(URL lraId, LRAContextBuilder lraContextBuilder, ContainerRequestContext requestContext) {
        lraContextBuilder.lraId(lraId).newlyStarted(true);
        requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraId.toExternalForm());
    }

    private long getTimeLimit() {
        TimeLimit timeLimit = resourceInfo.getResourceMethod().getAnnotation(TimeLimit.class);
        if (timeLimit == null) timeLimit = resourceInfo.getResourceClass().getAnnotation(TimeLimit.class);

        if (timeLimit == null) {
            return LRAConstants.DEFAULT_TIMELIMIT;
        }

        return timeLimit.unit().toMillis(timeLimit.limit());
    }

}
