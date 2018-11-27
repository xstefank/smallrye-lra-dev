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
import java.net.URL;

public class LRAResource {

    private URL compensateUrl;
    private URL completeUrl;
    private URL statusUrl;
    private URL forgetUrl;
    private URL leaveUrl;

    public LRAResource(URL compensateUrl, URL completeUrl, URL statusUrl, URL forgetUrl, URL leaveUrl) {
        this.compensateUrl = compensateUrl;
        this.completeUrl = completeUrl;
        this.statusUrl = statusUrl;
        this.forgetUrl = forgetUrl;
        this.leaveUrl = leaveUrl;
    }

    public LRAResource(Class<?> resourceClass, URI baseUri) throws MalformedURLException {
        Path resourcePath = resourceClass.getAnnotation(Path.class);

        UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(resourcePath != null ? resourcePath.value() : "");

        for (Method method : resourceClass.getMethods()) {

            if (method.getAnnotation(Complete.class) != null && completeUrl == null) {
                completeUrl = uriBuilder.clone().path(getPath(method)).build().toURL();
            } else if (method.getAnnotation(Compensate.class) != null && compensateUrl == null) {
                compensateUrl = uriBuilder.clone().path(getPath(method)).build().toURL();
            } else if (method.getAnnotation(Status.class) != null && statusUrl == null) {
                statusUrl = uriBuilder.clone().path(getPath(method)).build().toURL();
            } else if (method.getAnnotation(Forget.class) != null && forgetUrl == null) { 
                forgetUrl = uriBuilder.clone().path(getPath(method)).build().toURL();
            } else if (method.getAnnotation(Leave.class) != null && leaveUrl == null) {
                leaveUrl = uriBuilder.clone().path(getPath(method)).build().toURL();
            }
        }
    }

    private String getPath(Method method) {
        Path methodPath = method.getAnnotation(Path.class);
        return methodPath != null ? methodPath.value() : "";
    }

    public URL getCompleteUrl() {
        return completeUrl;
    }

    public URL getCompensateUrl() {
        return compensateUrl;
    }

    public URL getStatusUrl() {
        return statusUrl;
    }

    public URL getForgetUrl() {
        return forgetUrl;
    }

    public URL getLeaveUrl() {
        return leaveUrl;
    }

    @Override
    public String toString() {
        return "LRAResource{" +
                "completeUrl=" + completeUrl +
                ", compensateUrl=" + compensateUrl +
                ", statusUrl=" + statusUrl +
                ", forgetUrl=" + forgetUrl +
                ", leaveUrl=" + leaveUrl +
                '}';
    }

    public String asLinkHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append(createLink("compensate", compensateUrl)).append(",")
                .append(createLink("complete", completeUrl)).append(",")
                .append(createLink("forget", forgetUrl)).append(",")
                .append(createLink("leave", leaveUrl)).append(",")
                .append(createLink("status", statusUrl));

        return sb.toString();
    }

    private Link createLink(String kind, URL url) {
        return Link.fromUri(url.toExternalForm()).title(kind + "URI").rel(kind).type(MediaType.TEXT_PLAIN).build();
    }
}
