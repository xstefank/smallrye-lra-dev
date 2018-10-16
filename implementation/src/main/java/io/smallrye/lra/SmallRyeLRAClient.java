package io.smallrye.lra;

import io.smallrye.lra.utils.Utils;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.client.LRAInfo;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SmallRyeLRAClient implements LRAClient {

    private static final Logger log = Logger.getLogger(SmallRyeLRAClient.class);
    
    private URI coordinatorURI;
    private URI recoveryCoordinatorURI;
    
    @Inject
    private LRACoordinatorRESTClient coordinatorRESTClient;
    
    @Override
    public void setCoordinatorURI(URI uri) {
        this.coordinatorURI = uri;
    }

    @Override
    public void setRecoveryCoordinatorURI(URI uri) {
        this.recoveryCoordinatorURI = uri;
    }

    @Override
    public void close() {
        
    }

    @Override
    public URL startLRA(URL parentLRA, String clientID, Long timeout, TimeUnit unit) throws GenericLRAException {
        Response response = null;
        
        try {
            response = coordinatorRESTClient.startLRA(parentLRA != null ? Utils.extractLraId(parentLRA) : null, 
                    clientID, unit.toMillis(timeout));

            if (parentLRA != null && response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to start nested LRA for parent: " + parentLRA);
            }

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(null, response.getStatus(),
                        "Unexpected return code of LRA start for" + Utils.getFormattedString(parentLRA, clientID, timeout, unit), null);
            }

            return new URL(response.readEntity(String.class));
            
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null, response.getStatus(),
                    "Unable to start LRA for " + Utils.getFormattedString(parentLRA, clientID, timeout, unit), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
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

    private String endLRA(URL lraId, boolean confirm) {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = confirm ? coordinatorRESTClient.closeLRA(Utils.extractLraId(lraId)) : 
                                 coordinatorRESTClient.cancelLRA(Utils.extractLraId(lraId));

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to find LRA: " + lraId);
            }

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unexpected returned status code for LRA " 
                        + (confirm ? "confirmation" : "compensation"), null);
            }

            return response.readEntity(String.class);

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public List<LRAInfo> getActiveLRAs() throws GenericLRAException {
        return getLRAs(LRAStatus.ACTIVE);
    }

    @Override
    public List<LRAInfo> getAllLRAs() throws GenericLRAException {
        return getLRAs(null);
    }

    @Override
    public List<LRAInfo> getRecoveringLRAs() throws GenericLRAException {
        return getLRAs(LRAStatus.RECOVERING);
    }

    private List<LRAInfo> getLRAs(LRAStatus status) {
        Response response = null;

        try {
            response = status == null ? coordinatorRESTClient.getAllLRAs() : coordinatorRESTClient.getAllLRAs(status);

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(null, response.getStatus(),
                        String.format("Unable to get %s LRAs", status == null ? "all" : status), null);
            }

            return response.readEntity(new GenericType<List<LRAInfo>>() {});
        } catch (ProcessingException e) {
            throw new GenericLRAException(null, response != null ? response.getStatus() : -1,
                    String.format("Invalid content received for %s LRAs", status == null ? "all" : status), e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public Optional<CompensatorStatus> getStatus(URL lraId) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = coordinatorRESTClient.getLRA(Utils.extractLraId(lraId));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to get LRA status", null);
            }

            return Optional.of(response.readEntity(CompensatorStatus.class));
        } catch (ProcessingException e) {
            return Optional.empty();
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public Boolean isActiveLRA(URL lraId) throws GenericLRAException {
        return isCompensatorStatusIn(getStatus(lraId).orElseGet(() -> null), CompensatorStatus.Completing, CompensatorStatus.Compensating);
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
            response = coordinatorRESTClient.joinLRA(Utils.extractLraId(lraId), timelimit,
                    new ParticipantDefinition(completeUrl, compensateUrl, forgetUrl,
                            leaveUrl, statusUrl, compensatorData));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to join LRA", null);
            }

            return response.readEntity(String.class);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException {
        try {
            LRAParticipantResourceClass participantResourceClass = new LRAParticipantResourceClass(resourceClass);
            return joinLRA(lraId, 0L, 
                    participantResourceClass.getCompensateURL(baseUri),
                    participantResourceClass.getCompleteURL(baseUri),
                    participantResourceClass.getForgetURL(baseUri),
                    participantResourceClass.getLeaveURL(baseUri),
                    participantResourceClass.getStatusURL(baseUri),
                    compensatorData);
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new GenericLRAException(lraId, -1, e.getMessage(), e);
        } 
    }

    @Override
    public URL updateCompensator(URL recoveryUrl, URL compensateUrl, URL completeUrl, URL forgetUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
        Objects.requireNonNull(recoveryUrl);
        Response response = null;
        
        try {
            response = buildCompensatorRequest(recoveryUrl)
                    .put(Entity.json(new ParticipantDefinition(completeUrl, compensateUrl, forgetUrl,
                            null, statusUrl, compensatorData)));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(recoveryUrl, response.getStatus(), "Unable to update LRA participant", null);
            }
            
            return new URL(response.readEntity(String.class));
        } catch (MalformedURLException e) {
            throw new GenericLRAException(recoveryUrl, response.getStatus(), "Invalid response for participant update", e);
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public void leaveLRA(URL lraId, String body) throws GenericLRAException {
        Objects.requireNonNull(lraId);
        Objects.requireNonNull(body);
        Response response = null;
        
        try {
            response = coordinatorRESTClient.leaveLRA(Utils.extractLraId(lraId), body);

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
            response = coordinatorRESTClient.renewTimeLimit(Utils.extractLraId(lraId), unit.toMillis(limit));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to renew timelimit", null);
            }
        } finally {
            if (response != null) response.close();
        }
    }

    @Override
    public URL getCurrent() {
        //TODO programatic API
        return null;
    }
    
    private Invocation.Builder buildCompensatorRequest(URL recoveryUrl) {
        return ClientBuilder.newClient()
                .target(recoveryUrl.toString())
                .request(MediaType.TEXT_PLAIN);
    }

    private boolean isInvalidResponse(Response response) {
        return response.getStatus() != Response.Status.OK.getStatusCode();
    }
}
