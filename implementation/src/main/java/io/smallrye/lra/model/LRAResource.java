package io.smallrye.lra.model;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.TimeLimit;

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
    private Long compensateTimeLimit;

    public LRAResource(Class<?> resourceClass, URI baseUri) throws MalformedURLException {
        Path resourcePath = resourceClass.getAnnotation(Path.class);

        String  baseResourceUri = UriBuilder.fromUri(baseUri).path(resourcePath != null ? resourcePath.value() : "").build().toString();

        for (Method method : resourceClass.getMethods()) {

            if (method.getAnnotation(Complete.class) != null && completeUrl == null) {
                completeUrl = new URL(baseResourceUri + getPath(method));
            } else if (method.getAnnotation(Compensate.class) != null && compensateUrl == null) {
                compensateUrl = new URL(baseResourceUri + getPath(method));
                compensateTimeLimit = getTimeLimit(method);
            } else if (method.getAnnotation(Status.class) != null && statusUrl == null) {
                statusUrl = new URL(baseResourceUri + getPath(method));
            } else if (method.getAnnotation(Forget.class) != null && forgetUrl == null) { 
                forgetUrl = new URL(baseResourceUri + getPath(method));
            } else if (method.getAnnotation(Leave.class) != null && leaveUrl == null) {
                leaveUrl = new URL(baseResourceUri + getPath(method));
            }
        }
    }

    private long getTimeLimit(Method method) {
        TimeLimit timeLimit = method.getAnnotation(TimeLimit.class);

        if (timeLimit != null) {
            return timeLimit.unit().toMillis(timeLimit.limit());
        } else {
            return 0L;
        }
    }

    private String getPath(Method method) {
        Path methodPath = method.getAnnotation(Path.class);
        return methodPath != null ? escapePath(methodPath.value()): "";
    }

    private String escapePath(String value) {
        return value.startsWith("/") ? value : "/" + value;
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

    public Long getCompensateTimeLimit() {
        return compensateTimeLimit;
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
        return createLinkHeader(compensateUrl, completeUrl, forgetUrl, leaveUrl, statusUrl);
    }
    
    public static String createLinkHeader(URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl) {
        return createLink("compensate", compensateUrl) + "," +
                createLink("complete", completeUrl) + "," +
                createLink("forget", forgetUrl) + "," +
                createLink("leave", leaveUrl) + "," +
                createLink("status", statusUrl);
    }

    private static Link createLink(String kind, URL url) {
        return Link.fromUri(url.toExternalForm()).title(kind + "URI").rel(kind).type(MediaType.TEXT_PLAIN).build();
    }
}
