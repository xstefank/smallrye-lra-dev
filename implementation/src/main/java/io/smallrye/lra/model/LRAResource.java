package io.smallrye.lra.model;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.Status;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.net.URI;

public class LRAResource {
    
    private URI completeUri;
    private URI compensateUri;
    private URI statusUri;
    private URI forgetUri;
    private URI leaveUri;

    public LRAResource(URI completeUri, URI compensateUri, URI statusUri, URI forgetUri, URI leaveUri) {
        this.completeUri = completeUri;
        this.compensateUri = compensateUri;
        this.statusUri = statusUri;
        this.forgetUri = forgetUri;
        this.leaveUri = leaveUri;
    }

    public LRAResource(Class<?> resourceClass, URI baseUri) {
        LRAResource.LRAResourceBuilder resourceBuilder = LRAResource.builder();

        Path resourcePath = resourceClass.getAnnotation(Path.class);

        UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(resourcePath != null ? resourcePath.value() : "");

        for (Method method : resourceClass.getMethods()) {

            if (method.getAnnotation(Complete.class) != null) {
                this.completeUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Compensate.class) != null) {
                this.compensateUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Status.class) != null) {
                this.statusUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Forget.class) != null) { 
                this.forgetUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Leave.class) != null) {
                this.leaveUri = uriBuilder.clone().path(getPath(method)).build();
            }
        }
    }

    private String getPath(Method method) {
        Path methodPath = method.getAnnotation(Path.class);
        return methodPath != null ? methodPath.value() : "";
    }

    public URI getCompleteUri() {
        return completeUri;
    }

    public URI getCompensateUri() {
        return compensateUri;
    }

    public URI getStatusUri() {
        return statusUri;
    }

    public URI getForgetUri() {
        return forgetUri;
    }

    public URI getLeaveUri() {
        return leaveUri;
    }

    public static LRAResourceBuilder builder() {
        return new LRAResourceBuilder();
    }

    @Override
    public String toString() {
        return "LRAResource{" +
                "completeUri=" + completeUri +
                ", compensateUri=" + compensateUri +
                ", statusUri=" + statusUri +
                ", forgetUri=" + forgetUri +
                ", leaveUri=" + leaveUri +
                '}';
    }

    public static final class LRAResourceBuilder {

        private URI completeUri;
        private URI compensateUri;
        private URI statusUri;
        private URI forgetUri;
        private URI leaveUri;

        public LRAResourceBuilder completeUri(URI completeUri) {
            this.completeUri = completeUri;
            return this;
        }

        public LRAResourceBuilder compensateUri(URI compensateUri) {
            this.compensateUri = compensateUri;
            return this;
        }

        public LRAResourceBuilder statusUri(URI statusUri) {
            this.statusUri = statusUri;
            return this;
        }

        public LRAResourceBuilder forgetUri(URI forgetUri) {
            this.forgetUri = forgetUri;
            return this;
        }

        public LRAResourceBuilder leaveUri(URI leaveUri) {
            this.leaveUri = leaveUri;
            return this;
        }
        
        public LRAResource build() {
            return new LRAResource(completeUri, compensateUri, statusUri, forgetUri, leaveUri);
        }
        
    }
}
