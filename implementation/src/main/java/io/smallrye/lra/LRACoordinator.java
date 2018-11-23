package io.smallrye.lra;

import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.smallrye.lra.utils.LRAConstants.CLIENT_ID;
import static io.smallrye.lra.utils.LRAConstants.LRA_ID_PATH_PARAM;
import static io.smallrye.lra.utils.LRAConstants.PARENT_LRA;
import static io.smallrye.lra.utils.LRAConstants.STATUS;
import static io.smallrye.lra.utils.LRAConstants.TIMELIMIT;

@Path("/lra-coordinator")
@RegisterRestClient
public interface LRACoordinator {

    @POST
    @Path("/start")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    Response startLRA(@QueryParam(PARENT_LRA) @DefaultValue("") String parentLRA,
                      @QueryParam(CLIENT_ID) @DefaultValue("") String clientID,
                      @QueryParam(TIMELIMIT) @DefaultValue("0") Long timelimit);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllLRAs();

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllLRAs(@QueryParam(STATUS) String status);

    @GET
    @Path("/status/{" + LRA_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response isActiveLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/close")
    @Produces(MediaType.APPLICATION_JSON)
    Response closeLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    Response cancelLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);

    @GET
    @Path("/{" + LRA_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response joinLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId,
                     @QueryParam(TIMELIMIT) @DefaultValue("0") Long timelimit,
                     @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraHeader,
                     @HeaderParam("Link") String linkHeader,
                     String compensatorData);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/remove")
    @Produces(MediaType.APPLICATION_JSON)
    Response leaveLRA(@PathParam(LRA_ID_PATH_PARAM) String lraId, String body);

    @PUT
    @Path("/{" + LRA_ID_PATH_PARAM + "}/timelimit")
    @Produces(MediaType.APPLICATION_JSON)
    Response renewTimeLimit(@PathParam(LRA_ID_PATH_PARAM) String lraId, @QueryParam(TIMELIMIT) @DefaultValue("0") Long timelimit);
}
