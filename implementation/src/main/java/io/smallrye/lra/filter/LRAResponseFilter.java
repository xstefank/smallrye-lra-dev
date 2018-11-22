package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;

@Provider
public class LRAResponseFilter implements ContainerResponseFilter {

    private static final Logger log = Logger.getLogger(LRAResponseFilter.class);

    @Inject
    private LRAClient lraClient;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if (lraClient.getCurrent() != null) {
            responseContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraClient.getCurrent());
        }
        
        LRAContext lraContext = (LRAContext) requestContext.getProperty(LRAContext.CONTEXT_PROPERTY_NAME);

        if (lraContext == null) {
            // no LRA present
            return;
        }
        
        if (lraContext.isNewlyStarted()) {
            endLRA(lraContext, true);
        }

        if (lraContext.getCancelOn() != null && hasStatusIn(responseContext, lraContext.getCancelOn())) {
            endLRA(lraContext, false);
        }

        if (lraContext.getSuspendedLRA() != null) {
            responseContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraContext.getSuspendedLRA());
        }
    }

    private boolean hasStatusIn(ContainerResponseContext responseContext, Response.Status[] cancelOn) {
        return Arrays.stream(cancelOn)
                .map(Response.Status::getStatusCode)
                .anyMatch(code -> code == responseContext.getStatus());
    }

    private void endLRA(LRAContext lraContext, boolean complete) {
        try {
            String respone = complete ? lraClient.closeLRA(lraContext.getLraId()) : lraClient.cancelLRA(lraContext.getLraId());
            log.info("LRA ended successfully: " + respone);
        } catch (NotFoundException e) {
            log.warnf("LRA %s has already ended", lraContext.getLraId());
        } catch (GenericLRAException e) {
            log.errorf("Unable to end LRA %s; %s", lraContext.getLraId(), e.getCause().getMessage());
        }
    }
}
