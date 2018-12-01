package io.smallrye.lra;

import io.smallrye.lra.api.LRACoordinator;
import io.smallrye.lra.api.LRARecoveryCoordinator;
import io.smallrye.lra.model.LRAResource;
import io.smallrye.lra.model.SmallRyeLRAInfo;
import io.smallrye.lra.utils.Utils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.client.LRAInfo;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequestScoped
public class SmallRyeLRAClient implements LRAClient {

    private URL currentLRA;

    private LRACoordinator coordinator;
    private LRARecoveryCoordinator recoveryCoordinator;
    
    @Inject
    @ConfigProperty(name = "lra.coordinator.url")
    private String coordinatorUrl;
    
    @Inject
    @ConfigProperty(name = "lra.recovery.url")
    private String recoveryCoordinatorUrl;

    @PostConstruct
    public void init() throws MalformedURLException {
        coordinator = RestClientBuilder.newBuilder()
                .baseUrl(URI.create(coordinatorUrl).toURL()).build(LRACoordinator.class);

        recoveryCoordinator = RestClientBuilder.newBuilder()
                .baseUrl(URI.create(recoveryCoordinatorUrl).toURL()).build(LRARecoveryCoordinator.class);
    }

    public URL startLRA(URL parentLRA, String clientID, Long timeout) {
        return startLRA(parentLRA, clientID, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public URL startLRA(URL parentLRA, String clientID, Long timeout, TimeUnit unit) throws GenericLRAException {
        Response response = null;

        try {
            response = coordinator.startLRA(parentLRA != null ? parentLRA.toExternalForm() : null,
                    clientID, unit.toMillis(timeout));

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
        return startLRA(clientId, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public URL startLRA(String clientID, Long timeout, TimeUnit unit) throws GenericLRAException {
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

            return response.readEntity(String.class);
        } catch (WebApplicationException t) {
            throw new NotFoundException("Unable to find LRA: " + lraId);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public List<LRAInfo> getActiveLRAs() throws GenericLRAException {
        return getLRAs(null);
    }

    @Override
    public List<LRAInfo> getAllLRAs() throws GenericLRAException {
        return getLRAs(null);
    }

    private List<LRAInfo> getLRAs(CompensatorStatus status) {
        Response response = null;

        try {
            response = status == null ? coordinator.getAllLRAs() : coordinator.getAllLRAs(status.name());

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(null, response.getStatus(),
                        String.format("Unable to get %s LRAs", status == null ? "all" : status), null);
            }

            List<SmallRyeLRAInfo> smallRyeLRAInfos = response.readEntity(new GenericType<List<SmallRyeLRAInfo>>() {});
            return new ArrayList<>(smallRyeLRAInfos);
        } catch (ProcessingException e) {
            throw new GenericLRAException(null, response != null ? response.getStatus() : -1,
                    String.format("Invalid content received for %s LRAs", status == null ? "all" : status), e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public List<LRAInfo> getRecoveringLRAs() throws GenericLRAException {
        Response response = null;

        try {
            response = recoveryCoordinator.getRecoveringLRAs();

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(null, response.getStatus(),
                        "Unable to get recovering LRAs", null);
            }

            List<SmallRyeLRAInfo> smallRyeLRAInfos = response.readEntity(new GenericType<List<SmallRyeLRAInfo>>() {
            });
            return new ArrayList<>(smallRyeLRAInfos);
        } catch (ProcessingException e) {
            throw new GenericLRAException(null, response != null ? response.getStatus() : -1,
                    "Invalid content received for recovering LRAs", e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public Optional<CompensatorStatus> getStatus(URL lraId) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinator.getLRA(Utils.extractLraId(lraId));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to get LRA status", null);
            }

            SmallRyeLRAInfo lraInfo = response.readEntity(SmallRyeLRAInfo.class);
            return Optional.of(lraInfo.getStatus());
        } catch (ProcessingException e) {
            return Optional.empty();
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public Boolean isActiveLRA(URL lraId) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinator.isActiveLRA(Utils.extractLraId(lraId));

            return response.getStatus() == Response.Status.OK.getStatusCode();
        } catch (WebApplicationException e) {
            return false;
        } finally {
            if (response != null) response.close();
        }
    }

    private Boolean isCompensatorStatusIn(CompensatorStatus actual, CompensatorStatus... expected) {
        return Arrays.stream(expected).anyMatch(i -> Objects.equals(i, actual));
    }

    @Override
    public Boolean isCompensatedLRA(URL lraId) throws GenericLRAException {
        return getStatus(lraId).orElseGet(() -> null) == CompensatorStatus.Compensated;
    }

    @Override
    public Boolean isCompletedLRA(URL lraId) throws GenericLRAException {
        return getStatus(lraId).orElseGet(() -> null) == CompensatorStatus.Completed;
    }

    @Override
    public String joinLRA(URL lraId, Long timelimit, URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
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

            return response.readEntity(String.class);
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
    public String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException {
        try {
            LRAResource resource = new LRAResource(resourceClass, baseUri);
            return joinLRA(lraId, resource.getCompensateTimeLimit(),
                    resource.getCompensateUrl(),
                    resource.getCompleteUrl(),
                    resource.getForgetUrl(),
                    resource.getLeaveUrl(),
                    resource.getStatusUrl(),
                    compensatorData);
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new GenericLRAException(lraId, -1, e.getMessage(), e);
        }
    }

    @Override
    public URL updateCompensator(URL recoveryUrl, URL compensateUrl, URL completeUrl, URL forgetUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
        Objects.requireNonNull(recoveryUrl);
        Response response = null;
        
        String[] paths = recoveryUrl.toExternalForm().split("/");
        try {
            response = recoveryCoordinator.updateCompensator(paths[paths.length - 2], paths[paths.length - 1],
                    LRAResource.createLinkHeader(compensateUrl, completeUrl, forgetUrl, forgetUrl, statusUrl));
            
            return new URL(response.readEntity(String.class));
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null, response != null ? response.getStatus() : -1,
                    "Unable to update compensator " + recoveryUrl, e);
        } finally {
            if (response != null) response.close();
        }
    }

    public void leaveLRA(URL lraId, Class<?> resourceClass, URI baseUri) throws GenericLRAException {
        try {
            LRAResource lraResource = new LRAResource(resourceClass, baseUri);
            leaveLRA(lraId, lraResource.asLinkHeader());
        } catch (MalformedURLException e) {
            throw new GenericLRAException(lraId, -1, "Invalid urls produced by resource class", e);
        }
    }

    @Override
    public void leaveLRA(URL lraId, String body) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Objects.requireNonNull(body);
        Response response = null;

        try {
            response = coordinator.leaveLRA(Utils.extractLraId(lraId), body);

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to leave LRA: " + lraId);
            }

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new GenericLRAException(lraId, response.getStatus(),
                        "Unable to leave LRA because it is probably already completed or compensated", null);
            }

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Participant " + body + "is cannotleave LRA", null);
            }
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public void renewTimeLimit(URL lraId, long limit, TimeUnit unit) {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinator.renewTimeLimit(Utils.extractLraId(lraId), unit.toMillis(limit));

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

    @Override
    public void close() {
        currentLRA = null;
    }

    private Invocation.Builder buildCompensatorRequest(URL recoveryUrl) {
        return ClientBuilder.newClient()
                .target(recoveryUrl.toString())
                .request(MediaType.TEXT_PLAIN);
    }

    private boolean hasStatusCodeIn(Response response, Response.Status... statuses) {
        return Arrays.stream(statuses).anyMatch(s -> s.getStatusCode() == response.getStatus());
    }

    private boolean isInvalidResponse(Response response) {
        return response.getStatus() != Response.Status.OK.getStatusCode();
    }
}
