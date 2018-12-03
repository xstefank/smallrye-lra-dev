package io.smallrye.lra.filter;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class LRAClientRequestFilter implements ClientRequestFilter {

    private String coordinatorURL;
    private String recoveryURL;

    public LRAClientRequestFilter() {
        Config config = ConfigProvider.getConfig();
        coordinatorURL = config.getValue("lra.coordinator.url", String.class);
        recoveryURL = config.getValue("lra.recovery.url", String.class);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {

        String uri = requestContext.getUri().toString();
        if (uri.startsWith(coordinatorURL) || uri.startsWith(recoveryURL) ||
                requestContext.getHeaders().containsKey(LRAClient.LRA_HTTP_HEADER)) {
            return;
        }

        // necessary as RestEasy currently requires ClientRequestFilter
        // to be registered manually
        LRAClient lraClient = CDI.current().select(LRAClient.class).get();
        if (lraClient.getCurrent() != null) {
            requestContext.getHeaders().putSingle(LRAClient.LRA_HTTP_HEADER, lraClient.getCurrent());
        }
    }
}
