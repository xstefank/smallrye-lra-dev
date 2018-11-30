package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.client.LRAClient;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LRAClientRequestFilter implements ClientRequestFilter {
        
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // necessary as RestEasy currently requires ClientRequestFilter
        // to be registered manually
        LRAClient lraClient = CDI.current().select(LRAClient.class).get();

        if (lraClient.getCurrent() != null) {
            requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraClient.getCurrent());
        }
    }
}
