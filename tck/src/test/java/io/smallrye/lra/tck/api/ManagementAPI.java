package io.smallrye.lra.tck.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.smallrye.lra.model.LRAConstants.LRA_ID_PATH_PARAM;
import static io.smallrye.lra.model.LRAConstants.STATUS;

@Path("/lra-coordinator")
@RegisterRestClient
public interface ManagementAPI {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllLRAs();

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllLRAs(@QueryParam(STATUS) String status);

    @GET
    @Path("/{" + LRA_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);
}
