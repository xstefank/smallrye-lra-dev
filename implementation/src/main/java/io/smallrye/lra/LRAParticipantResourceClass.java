package io.smallrye.lra;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.Status;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class LRAParticipantResourceClass {
    
    private Method completeMethod;
    private Method compensateMethod;
    private Method forgetMethod;
    private Method leaveMethod;
    private Method statusMethod;

    public LRAParticipantResourceClass(Class<?> resourceClass) {
        for (Method method : resourceClass.getMethods()) {
            if (method.getAnnotation(Complete.class) != null) {
                if (completeMethod != null) {
                    throw new IllegalArgumentException("Only one method can be annotated by @Complete annotation");
                }
                
                completeMethod = method;
            } else if (method.getAnnotation(Compensate.class) != null) {
                if (compensateMethod != null) {
                    throw new IllegalArgumentException("Only one method can be annotated by @Compensate annotation");
                }
                
                compensateMethod = method;
            } else if (method.getAnnotation(Forget.class) != null) {
                if (forgetMethod != null) {
                    throw new IllegalArgumentException("Only one method can be annotated by @Forget annotation");
                }
                
                forgetMethod = method;
            } else if (method.getAnnotation(Leave.class) != null) {
                if (leaveMethod != null) {
                    throw new IllegalArgumentException("Only one method can be annotated by @Leave annotation");
                }
                
                leaveMethod = method;
            } else if (method.getAnnotation(Status.class) != null) {
                if (statusMethod != null) {
                    throw new IllegalArgumentException("Only one method can be annotated be @Status annotation");
                }
                
                statusMethod = method;
            }
        }
    }

    public URL getCompensateURL(URI baseUri) throws MalformedURLException {
        return UriBuilder.fromUri(baseUri).path(compensateMethod.getAnnotation(Path.class).value()).build().toURL();
    }

    public URL getCompleteURL(URI baseUri) throws MalformedURLException {
        return UriBuilder.fromUri(baseUri).path(completeMethod.getAnnotation(Path.class).value()).build().toURL();
    }

    public URL getForgetURL(URI baseUri) throws MalformedURLException {
        return UriBuilder.fromUri(baseUri).path(forgetMethod.getAnnotation(Path.class).value()).build().toURL();
    }

    public URL getLeaveURL(URI baseUri) throws MalformedURLException {
        return UriBuilder.fromUri(baseUri).path(leaveMethod.getAnnotation(Path.class).value()).build().toURL();
    }

    public URL getStatusURL(URI baseUri) throws MalformedURLException {
        return UriBuilder.fromUri(baseUri).path(statusMethod.getAnnotation(Path.class).value()).build().toURL();
    }
}
