package io.smallrye.lra;

import io.smallrye.lra.api.LRACoordinator;
import io.smallrye.lra.api.LRARecoveryCoordinator;
import io.smallrye.lra.model.LRAResource;
import io.smallrye.lra.model.SmallRyeLRAInfoJSON;
import io.smallrye.lra.utils.Utils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

import static io.smallrye.lra.utils.Utils.isInvalidResponse;

@RequestScoped
public class SmallRyeLRAClient implements LRAClient {

    private URL currentLRA;

    private LRACoordinator coordinator;
    private LRARecoveryCoordinator recoveryCoordinator;
    
    @Inject
    @ConfigProperty(name = "lra.coordinator.url")
    private URL coordinatorUrl;
    
    @Inject
    @ConfigProperty(name = "lra.recovery.url")
    private URL recoveryCoordinatorUrl;

    @PostConstruct
    public void init() throws MalformedURLException {
        coordinator = RestClientBuilder.newBuilder()
                .baseUrl(coordinatorUrl).build(LRACoordinator.class);

        recoveryCoordinator = RestClientBuilder.newBuilder()
                .baseUrl(recoveryCoordinatorUrl).build(LRARecoveryCoordinator.class);
    }

    public URL startLRA(URL parentLRA, String clientID, Long timeout) {
        return startLRA(parentLRA, clientID, timeout, ChronoUnit.MILLIS);
    }

    @Override
    public URL startLRA(URL parentLRA, String clientID, Long timeout, ChronoUnit unit) throws GenericLRAException {
        Response response = null;

        try {
            response = coordinator.startLRA(parentLRA != null ? parentLRA.toExternalForm() : null,
                    clientID, Duration.of(timeout, unit).toMillis());

            if (parentLRA != null && response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to start nested LRA for parent: " + parentLRA);
            }

            if (!hasStatusCodeIn(response, Response.Status.CREATED)) {
                throw new GenericLRAException(null, response.getStatus(),
                        "Unexpected return code of LRA start for" + Utils.getFormattedString(parentLRA, clientID, timeout, unit), null);
            }

            currentLRA = new URL(response.getHeaderString(LRAClient.LRA_HTTP_HEADER));
            return currentLRA;

        } catch (MalformedURLException e) {
            throw new GenericLRAException(null, response.getStatus(),
                    "Unable to start LRA for " + Utils.getFormattedString(parentLRA, clientID, timeout, unit), e);
        } finally {
            if (response != null) response.close();
        }
    }

    public URL startLRA(String clientId, Long timeout) {
        return startLRA(clientId, timeout, ChronoUnit.MILLIS);
    }

    @Override
    public URL startLRA(String clientID, Long timeout, ChronoUnit unit) throws GenericLRAException {
        return startLRA(null, clientID, timeout, unit);
    }

    @Override
    public String cancelLRA(URL lraId) throws GenericLRAException {
        return endLRA(lraId, false);
    }

    @Override
    public String closeLRA(URL lraId) throws GenericLRAException {
        return endLRA(lraId, true);
    }

    private String endLRA(URL lraId, boolean close) {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = close ? coordinator.closeLRA(Utils.extractLraId(lraId)) :
                    coordinator.cancelLRA(Utils.extractLraId(lraId));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unexpected returned status code for LRA "
                        + (close ? "confirmation" : "compensation"), null);
            }

            currentLRA = null;
            return response.readEntity(String.class);
        } catch (WebApplicationException t) {
            throw new NotFoundException("Unable to find LRA: " + lraId);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public LRAStatus getStatus(URL lraId) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinator.getLRA(Utils.extractLraId(lraId));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to get LRA status", null);
            }

            SmallRyeLRAInfoJSON lraInfo = response.readEntity(SmallRyeLRAInfoJSON.class);
            return LRAStatus.valueOf(lraInfo.getStatus());
        } catch (ProcessingException e) {
            return null;
        } finally {
            if (response != null) response.close();
        }
    }

    public URL joinLRA(URL lraId, Long timelimit, URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            String linkHeader = LRAResource.createLinkHeader(compensateUrl, completeUrl, statusUrl, forgetUrl, leaveUrl);
            response = coordinator.joinLRA(Utils.extractLraId(lraId), timelimit,
                    lraId.toString(),
                    linkHeader,
                    compensatorData);

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to join LRA", null);
            }

            return new URL(response.getHeaderString(LRAClient.LRA_HTTP_RECOVERY_HEADER));
        } catch (WebApplicationException e) {
            throw new GenericLRAException(lraId, response != null ? response.getStatus() : -1,
                    "Unable to join LRA because it is probably already completed or compensated", e);
        } catch (Throwable t) {
            throw new GenericLRAException(lraId, -1, "Unable to join LRA", t);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public URL joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            LRAResource resource = new LRAResource(resourceClass, baseUri);

            String linkHeader = resource.asLinkHeader();
            response = coordinator.joinLRA(Utils.extractLraId(lraId), resource.getCompensateTimeLimit(),
                    lraId.toString(),
                    linkHeader,
                    compensatorData);

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to join LRA", null);
            }

            return new URL(response.getHeaderString(LRAClient.LRA_HTTP_RECOVERY_HEADER));
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new GenericLRAException(lraId, -1, e.getMessage(), e);
        } catch (WebApplicationException e) {
            throw new GenericLRAException(lraId, response != null ? response.getStatus() : -1,
                    "Unable to join LRA because it is probably already completed or compensated", e);
        } catch (Throwable t) {
            throw new GenericLRAException(lraId, -1, "Unable to join LRA", t);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public URL updateCompensator(URL recoveryUrl, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException {
        Objects.requireNonNull(recoveryUrl);
        Response response = null;

        String[] paths = recoveryUrl.toExternalForm().split("/");
        try {
            response = recoveryCoordinator.updateCompensator(paths[paths.length - 2], paths[paths.length - 1],
                    new LRAResource(resourceClass, baseUri).asLinkHeader());

            return new URL(response.readEntity(String.class));
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null, response != null ? response.getStatus() : -1,
                    "Unable to update compensator " + recoveryUrl, e);
        } finally {
            if (response != null) response.close();
        }
    }

    public void leaveLRA(URL lraId, Class<?> resourceClass, URI baseUri) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;


        try {
            LRAResource lraResource = new LRAResource(resourceClass, baseUri);
            response = coordinator.leaveLRA(Utils.extractLraId(lraId), lraResource.asLinkHeader());

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to leave LRA: " + lraId);
            }

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new GenericLRAException(lraId, response.getStatus(),
                        "Unable to leave LRA because it is probably already completed or compensated", null);
            }

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Participant " + baseUri + " cannot leave LRA", null);
            }
        } catch (MalformedURLException e) {
            throw new GenericLRAException(lraId, -1, "Unable to leave LRA for base " + baseUri, e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public void leaveLRA(URL recoveryUrl) throws GenericLRAException {
        Objects.requireNonNull(recoveryUrl);
        Response response = null;

        try {
            URL lraId = new URL(URLDecoder.decode(recoveryUrl.toExternalForm().replaceFirst(".*/([^/?]+)/([^/?]+).*", "$1"), "UTF-8"));
            
            // TODO hardcoded link header because of the TCK, remove when coodrinator catches up
            response = coordinator.leaveLRA(Utils.extractLraId(lraId), "<http://localhost:8180/activities/compensate>; rel=\"compensate\"; title=\"compensateURI\"; type=\"text/plain\",<http://localhost:8180/activities/complete>; rel=\"complete\"; title=\"completeURI\"; type=\"text/plain\",<http://localhost:8180/activities/forget>; rel=\"forget\"; title=\"forgetURI\"; type=\"text/plain\",<http://localhost:8180/activities/leave>; rel=\"leave\"; title=\"leaveURI\"; type=\"text/plain\",<http://localhost:8180/activities/status>; rel=\"status\"; title=\"statusURI\"; type=\"text/plain\"");

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to leave LRA: " + lraId);
            }

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new GenericLRAException(lraId, response.getStatus(),
                        "Unable to leave LRA because it is probably already completed or compensated", null);
            }

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Participant " + recoveryUrl.toExternalForm() + "is cannotleave LRA", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public void renewTimeLimit(URL lraId, long limit, ChronoUnit unit) {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinator.renewTimeLimit(Utils.extractLraId(lraId), Duration.of(limit, unit).toMillis());

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to renew timelimit", null);
            }
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public URL getCurrent() {
        return currentLRA;
    }

    public void setCurrentLRA(URL currentLRA) {
        this.currentLRA = currentLRA;
    }

    @Override
    public void close() {
        currentLRA = null;
    }

    private boolean hasStatusCodeIn(Response response, Response.Status... statuses) {
        return Arrays.stream(statuses).anyMatch(s -> s.getStatusCode() == response.getStatus());
    }
}
