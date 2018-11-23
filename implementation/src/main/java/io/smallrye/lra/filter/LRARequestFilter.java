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

        boolean incomingLRAPresent = lraId != null;
        
        switch (lra.value()) {
            
            case REQUIRED:
                if (!incomingLRAPresent) {
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                } else if (nested) {
                    lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                }
                break;
                
            case REQUIRES_NEW:
                if (!incomingLRAPresent) {
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                } else {
                    suspendLRA(requestContext, lraContextBuilder, lraId);
                    lraId = startNewLRA(lraContextBuilder, requestContext);
                    requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraId.toExternalForm());
                }
                break;
                
            case MANDATORY:
                if (!incomingLRAPresent) {
                    requestContext.abortWith(Response
                            .status(Response.Status.PRECONDITION_FAILED)
                            .entity(Entity.text("LRA type MANDATORY requires to be called inside of a LRA Context"))
                            .build());
                } else if (nested) {
                    lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                }
                break;
                
            case SUPPORTS:
                if (nested) {
                    if (incomingLRAPresent) {
                        lraId = startNewLRA(lraId, lraContextBuilder, requestContext);
                    } else {
                        lraId = startNewLRA(lraContextBuilder, requestContext);
                    }
                }
                break;
                
            case NOT_SUPPORTED:
                if (incomingLRAPresent) {
                    suspendLRA(requestContext, lraContextBuilder, lraId);
                }
                break;
                
            case NEVER:
                if (incomingLRAPresent) {
                    requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED)
                            .entity(Entity.text("LRA type NEVER cannot be called inside of a LRA Context"))
                            .build());
                }
                break;

        }

        if (lra.join() && lraId != null) {
            lraClient.joinLRA(lraId, resourceInfo.getResourceClass(), requestContext.getUriInfo().getBaseUri(), null);
        }

        if (lra.cancelOnFamily().length != 0) {
            lraContextBuilder.cancelOnFamily(lra.cancelOnFamily());
        }

        if (lra.cancelOn().length != 0) {
            lraContextBuilder.cancelOn(lra.cancelOn());
        }

        requestContext.setProperty(LRAContext.CONTEXT_PROPERTY_NAME, lraContextBuilder.build());
    }

    private void suspendLRA(ContainerRequestContext requestContext, LRAContextBuilder lraContextBuilder, URL lraId) {
        lraContextBuilder.suspend(lraId);
        requestContext.getHeaders().remove(LRAClient.LRA_HTTP_HEADER);
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
