package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LRAResponseFilter implements ContainerResponseFilter {

    private static final Logger log = Logger.getLogger(LRAResponseFilter.class);

    @Inject
    private LRAClient lraClient;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LRAContext lraContext = (LRAContext) requestContext.getProperty(LRAContext.CONTEXT_PROPERTY_NAME);

        if (lraContext == null) {
            // no LRA present
            return;
        }

        responseContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraContext.getLraId());

        if (lraContext.isNewlyStarted()) {
            closeLRA(lraContext);
        }

        if (lraContext.getSuspendedLRA() != null) {
            responseContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraContext.getSuspendedLRA());
        }
    }

    private void closeLRA(LRAContext lraContext) {
        try {
            lraClient.closeLRA(lraContext.getLraId());
        } catch (NotFoundException e) {
            log.infof("LRA %s is already closed", lraContext.getLraId());
        } catch (GenericLRAException e) {
            log.infof("Unable to close LRA %s; %s", lraContext.getLraId(), e.getCause().getMessage());
        }
    }
}
