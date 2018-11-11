package io.smallrye.lra.filter;

import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

@Provider
public class LRARequestFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;
    
    @Inject
    private LRAClient lraClient;
    
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        LRA lra = resourceMethod.getAnnotation(LRA.class);

        if (lra == null) {
            return;
        }

        String lraHeader = ctx.getHeaderString(LRAClient.LRA_HTTP_HEADER);
        URL lraId = lraHeader != null ? new URL(lraHeader) : null;
        boolean shouldJoin = lra.join();

        switch (lra.value()) {
            /**
             *  If called outside a LRA context a JAX-RS filter will begin a new
             *  LRA for the duration of the method call and when the call completes
             *  another JAX-RS filter will complete the LRA.
             */
            case REQUIRED:
                if (lraHeader != null) {
                    System.out.println(lraHeader);
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
            case REQUIRES_NEW:
                break;

                    /**
                     *  If called outside a transaction context, the method call will return
                     *  with a 412 Precondition Failed HTTP status code
                     *
                     *  If called inside a transaction context the bean method execution will
                     *  then continue within that context.
                     */
            case MANDATORY:
                break;

                    /**
                     *  If called outside a LRA context the bean method execution
                     *  must then continue outside a LRA context.
                     *
                     *  If called inside a LRA context the managed bean method execution
                     *  must then continue inside this LRA context.
                     */
            case SUPPORTS:
                break;

                    /**
                     *  The bean method is executed without a LRA context. If a context is
                     *  present on entry then it is suspended and then resumed after the
                     *  execution has completed.
                     */
            case NOT_SUPPORTED:
                break;

                    /**
                     *  If called outside a LRA context the managed bean method execution
                     *  must then continue outside a LRA context.
                     *
                     *  If called inside a LRA context the method is not executed and a
                     *  <code>412 Precondition Failed</code> HTTP status code is returned
                     *  to the caller.
                     */
            case NEVER:
                break;

        }

        if (shouldJoin && lraId != null) {
            lraClient.joinLRA(lraId, resourceInfo.getResourceClass(), uriInfo.getBaseUri(), null);
        }
    }

}
