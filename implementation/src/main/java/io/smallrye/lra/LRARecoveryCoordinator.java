package io.smallrye.lra;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.smallrye.lra.utils.LRAConstants.LRA_ID_PATH_PARAM;
import static io.smallrye.lra.utils.LRAConstants.RECOVERY_ID_PATH_PARAM;

@Path("/lra-recovery-coordinator")
@RegisterRestClient
public interface LRARecoveryCoordinator {

    @GET
    @Path("/recovery")
    @Produces(MediaType.APPLICATION_JSON)
    Response getRecoveringLRAs();
    
    @GET
    @Path("/{" + LRA_ID_PATH_PARAM + "}/{" + RECOVERY_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCompensator(@PathParam(LRA_ID_PATH_PARAM) String lraId, @PathParam(RECOVERY_ID_PATH_PARAM) String recoveryId);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/{" + RECOVERY_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response updateCompensator(@PathParam(LRA_ID_PATH_PARAM) String lraId, @PathParam(RECOVERY_ID_PATH_PARAM) String recoveryId, String body);
}
