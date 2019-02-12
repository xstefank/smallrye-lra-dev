package io.smallrye.lra.tck;

import io.smallrye.lra.model.SmallRyeLRAInfoJSON;
import io.smallrye.lra.tck.api.ManagementAPI;
import io.smallrye.lra.tck.model.SmallRyeLRAInfo;
import io.smallrye.lra.utils.Utils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.tck.LRAInfo;
import org.eclipse.microprofile.lra.tck.spi.ManagementSPI;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.smallrye.lra.utils.Utils.isInvalidResponse;

@ApplicationScoped
public class SmallRyeManagementSPI implements ManagementSPI {
    
    private ManagementAPI management;

    @Inject
    @ConfigProperty(name = "lra.coordinator.url")
    private URL managementURL;

    @PostConstruct
    public void init() throws MalformedURLException {
        management = RestClientBuilder.newBuilder()
                .baseUrl(managementURL).build(ManagementAPI.class);
    }


    @Override
    public LRAInfo getStatus(URL lraId) throws NotFoundException {
        Objects.requireNonNull(lraId);
        Response response = null;

        try {
            response = management.getLRA(Utils.extractLraId(lraId));

            if (isInvalidResponse(response)) {
                throw new GenericLRAException(lraId, response.getStatus(), "Unable to get LRA", null);
            }

            return (LRAInfo) response.readEntity(SmallRyeLRAInfoJSON.class);
        } catch (ProcessingException e) {
            return null;
        } finally {
            if (response != null) response.close();
        }

    }

    @Override
    public List<LRAInfo> getLRAs(LRAStatus status) {
        Response response = null;

        try {
            response = status == null ? management.getAllLRAs() : management.getAllLRAs(status.name());

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
}
