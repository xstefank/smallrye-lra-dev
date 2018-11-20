package io.smallrye.lra.model;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.Status;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class LRAResource {

    private URI compensateUri;
    private URI completeUri;
    private URI statusUri;
    private URI forgetUri;
    private URI leaveUri;

    public LRAResource(URL compensateUrl, URL completeUrl, URL statusUrl, URL forgetUrl, URL leaveUrl) throws URISyntaxException {
        this.compensateUri = compensateUrl.toURI();
        this.completeUri = completeUrl.toURI();
        this.statusUri = statusUrl.toURI();
        this.forgetUri = forgetUrl.toURI();
        this.leaveUri = leaveUrl.toURI();
    }

    public LRAResource(URI compensateUri, URI completeUri, URI statusUri, URI forgetUri, URI leaveUri) {
        this.compensateUri = compensateUri;
        this.completeUri = completeUri;
        this.statusUri = statusUri;
        this.forgetUri = forgetUri;
        this.leaveUri = leaveUri;
    }

    public LRAResource(Class<?> resourceClass, URI baseUri) {
        LRAResource.LRAResourceBuilder resourceBuilder = LRAResource.builder();

        Path resourcePath = resourceClass.getAnnotation(Path.class);

        UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(resourcePath != null ? resourcePath.value() : "");

        for (Method method : resourceClass.getMethods()) {

            if (method.getAnnotation(Complete.class) != null && completeUri == null) {
                completeUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Compensate.class) != null && compensateUri == null) {
                compensateUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Status.class) != null && statusUri == null) {
                statusUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Forget.class) != null && forgetUri == null) { 
                forgetUri = uriBuilder.clone().path(getPath(method)).build();
            } else if (method.getAnnotation(Leave.class) != null && leaveUri == null) {
                leaveUri = uriBuilder.clone().path(getPath(method)).build();
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

    public String asLinkHeader() throws MalformedURLException {
        StringBuilder sb = new StringBuilder();

        sb.append(createLink("compensate", compensateUri.toURL())).append(",")
                .append(createLink("complete", completeUri.toURL())).append(",")
                .append(createLink("forget", forgetUri.toURL())).append(",")
                .append(createLink("leave", leaveUri.toURL())).append(",")
                .append(createLink("status", statusUri.toURL()));

        return sb.toString();
    }

    private Link createLink(String kind, URL url) {
        return Link.fromUri(url.toExternalForm()).title(kind + "URI").rel(kind).type(MediaType.TEXT_PLAIN).build();
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
