package io.smallrye.lra.filter;

import io.smallrye.lra.SmallRyeLRAClient;
import io.smallrye.lra.model.LRAConstants;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.NestedLRA;
import org.eclipse.microprofile.lra.annotation.Status;
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
import java.time.Duration;

@Provider
public class LRAContainerRequestFilter implements ContainerRequestFilter {

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

        LRA lra = resourceMethod.getAnnotation(LRA.class);

        if (lra == null) {
            boolean isHelperMethod = processHelperLRAAnnotations(resourceMethod, lraId, requestContext);
            if (!isHelperMethod && lraId != null) {
                //invoking non LRA aware resource method with LRA context present so suspend LRA
                suspendLRA(requestContext, lraContextBuilder, lraId);
                lraClient.setCurrentLRA(lraId);
            }
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
                    lraId = null;
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

        if (lraId != null) {
            URL receiverUrl = lraClient.joinLRA(lraId, resourceInfo.getResourceClass(), requestContext.getUriInfo().getBaseUri(), null);
            requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_RECOVERY_HEADER, receiverUrl.toExternalForm());
        }

        if (lra.cancelOnFamily().length != 0) {
            lraContextBuilder.cancelOnFamily(lra.cancelOnFamily());
        }

        if (lra.cancelOn().length != 0) {
            lraContextBuilder.cancelOn(lra.cancelOn());
        }

        lraContextBuilder.lraId(lraId);
        requestContext.setProperty(LRAContext.CONTEXT_PROPERTY_NAME, lraContextBuilder.build());
    }

    private void suspendLRA(ContainerRequestContext requestContext, LRAContextBuilder lraContextBuilder, URL lraId) {
        lraContextBuilder.suspend(lraId);
        requestContext.getHeaders().remove(LRAClient.LRA_HTTP_HEADER);
    }

    private boolean processHelperLRAAnnotations(Method method, URL lraId, ContainerRequestContext requestContext) {
        if (method.isAnnotationPresent(Leave.class)) {
            lraClient.leaveLRA(lraId, resourceInfo.getResourceClass(), requestContext.getUriInfo().getBaseUri());
            return true;
        } else return method.isAnnotationPresent(Compensate.class) ||
                method.isAnnotationPresent(Complete.class) ||
                method.isAnnotationPresent(Status.class) ||
                method.isAnnotationPresent(Forget.class);

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
        LRA lra = resourceInfo.getResourceMethod().getAnnotation(LRA.class);

        if (lra == null) {
            return LRAConstants.DEFAULT_TIMELIMIT;
        }

        return Duration.of(lra.timeLimit(), lra.timeUnit()).toMillis();
    }

}
