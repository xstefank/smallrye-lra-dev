package io.smallrye.lra;

import io.smallrye.lra.utils.Utils;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.client.LRAInfo;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
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
        Objects.requireNonNull(lraId);
        Response response = null;
        
        try {
            response = coordinatorRESTClient.cancelLRA(Utils.extractLraId(lraId));

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException("Unable to find LRA: " + lraId);
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unexpected return code for LRA compensation", null);
            }

            return response.readEntity(String.class);
            
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public String closeLRA(URL lraId) throws GenericLRAException {
        return null;
    }

    @Override
    public List<LRAInfo> getActiveLRAs() throws GenericLRAException {
        return null;
    }

    @Override
    public List<LRAInfo> getAllLRAs() throws GenericLRAException {
        return null;
    }

    @Override
    public List<LRAInfo> getRecoveringLRAs() throws GenericLRAException {
        return null;
    }

    @Override
    public Optional<CompensatorStatus> getStatus(URL lraId) throws GenericLRAException {
        return Optional.empty();
    }

    @Override
    public Boolean isActiveLRA(URL lraId) throws GenericLRAException {
        return null;
    }

    @Override
    public Boolean isCompensatedLRA(URL lraId) throws GenericLRAException {
        return null;
    }

    @Override
    public Boolean isCompletedLRA(URL lraId) throws GenericLRAException {
        return null;
    }

    @Override
    public String joinLRA(URL lraId, Long timelimit, URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
        return null;
    }

    @Override
    public String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException {
        return null;
    }

    @Override
    public URL updateCompensator(URL recoveryUrl, URL compensateUrl, URL completeUrl, URL forgetUrl, URL statusUrl, String compensatorData) throws GenericLRAException {
        return null;
    }

    @Override
    public void leaveLRA(URL lraId, String body) throws GenericLRAException {

    }

    @Override
    public void renewTimeLimit(URL lraId, long limit, TimeUnit unit) {

    }

    @Override
    public URL getCurrent() {
        return null;
    }

    @Override
    public void setCurrentLRA(URL lraId) {

    }
}
