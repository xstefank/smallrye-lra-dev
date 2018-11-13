package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;

@Provider
public class LRAResponseFilter implements ContainerResponseFilter {

    @Inject
    private LRAClient lraClient;
    
    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        URL lraId = (URL) containerRequestContext.getProperty(LRAClient.LRA_HTTP_HEADER);
        System.out.println(lraId);
//        if (lraId != null) {
//            lraClient.closeLRA(lraId);
//        }
    }
}
