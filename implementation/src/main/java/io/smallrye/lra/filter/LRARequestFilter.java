package io.smallrye.lra.filter;

import io.smallrye.lra.model.LRAResource;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;

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

        if (shouldJoin) {
            LRAResource lraResource = createLRAResource(resourceInfo.getResourceClass());
        }
    }

    private LRAResource createLRAResource(Class<?> clazz) {
        LRAResource.LRAResourceBuilder resourceBuilder = LRAResource.builder();

        Path resourcePath = clazz.getAnnotation(Path.class);

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(resourcePath != null ? resourcePath.value() : "");

        for (Method method : clazz.getMethods()) {
            
            if (method.getAnnotation(LRA.class) != null) {
                resourceBuilder.lraUri(uriBuilder.path(getPath(method)).build());
            } else if (method.getAnnotation(Complete.class) != null) {
                resourceBuilder.completeUri(uriBuilder.path(getPath(method)).build());
            } else if (method.getAnnotation(Compensate.class) != null) {
                resourceBuilder.compensateUri(uriBuilder.path(getPath(method)).build());
            } else if (method.getAnnotation(Status.class) != null) {
                resourceBuilder.statusUri(uriBuilder.path(getPath(method)).build());
            } else if (method.getAnnotation(Forget.class) != null) {
                resourceBuilder.forgetUri(uriBuilder.path(getPath(method)).build());
            } else if (method.getAnnotation(Leave.class) != null) {
                resourceBuilder.leaveUri(uriBuilder.path(getPath(method)).build());
            }
        }

        LRAResource build = resourceBuilder.build();
        System.out.println("XXXXXXXXXXXXXXXXXXX" + build);
        return build;
    }

    private String getPath(Method method) {
        Path methodPath = method.getAnnotation(Path.class);
        return methodPath != null ? methodPath.value() : "";
    }


}
