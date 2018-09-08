package io.smallrye.lra;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.smallrye.lra.utils.LRAConstants.CANCEL;
import static io.smallrye.lra.utils.LRAConstants.CLOSE;
import static io.smallrye.lra.utils.LRAConstants.LRA_ID_PATH_PARAM;

@Path("/lra-coordinator")
public interface LRACoordinatorRESTClient {
    
    @POST
    @Path("/start")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    Response startLRA(@QueryParam("ClientId") @DefaultValue("") String clientID,
                      @QueryParam("TimeLimit") @DefaultValue("0") Long timelimit,
                      @QueryParam("ParentLRA") @DefaultValue("") String parentLRA);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/close")
    @Produces(MediaType.APPLICATION_JSON)
    Response closeLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    Response cancelLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);
    
}
