package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LRAResponseFilter implements ContainerResponseFilter {

    @Inject
    private LRAClient lraClient;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        LRAContext lraContext = (LRAContext) containerRequestContext.getProperty(LRAContext.CONTEXT_PROPERTY_NAME);
        System.out.println(lraContext);
//        if (lraId != null) {
//            lraClient.closeLRA(lraId);
//        }
    }
}
