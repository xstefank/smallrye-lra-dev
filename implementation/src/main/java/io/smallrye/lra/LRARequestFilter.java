package io.smallrye.lra;

import org.eclipse.microprofile.lra.annotation.LRA;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
public class LRARequestFilter implements ContainerRequestFilter {

    @Context
    protected ResourceInfo resourceInfo;
    
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        LRA lra = resourceMethod.getAnnotation(LRA.class);

        if (lra != null) {
            System.out.println(lra.value());
        }
    }
}
