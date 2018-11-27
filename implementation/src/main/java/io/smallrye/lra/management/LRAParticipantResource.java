package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.print.attribute.standard.Media;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URL;

import static io.smallrye.lra.model.LRAConstants.LRA_ID_PATH_PARAM;
import static io.smallrye.lra.model.LRAConstants.PARTICIPANT_ID_PATH_PARAM;

@Path("/lra-participant")
public class LRAParticipantResource {

    private static final String PARTICIPANT_PATH = "/{" + LRA_ID_PATH_PARAM + "}/{" + PARTICIPANT_ID_PATH_PARAM + "}";

    @Inject
    private SmallRyeLRAManagement lraManagement;

    @PUT
    @Path(PARTICIPANT_PATH + "/compensate")
    @Produces(MediaType.TEXT_PLAIN)
    @Compensate
    public Response compensate(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) throws MalformedURLException {
        URL lra = new URL(lraIdHeader);
        return lraManagement.getParticipant(participantId, lra, data.getBytes()).compensate(lra);
    }

    @PUT
    @Path(PARTICIPANT_PATH + "/complete")
    @Produces(MediaType.TEXT_PLAIN)
    @Complete
    public Response complete(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) throws MalformedURLException {
        URL lra = new URL(lraIdHeader);
        return lraManagement.getParticipant(participantId, lra, data.getBytes()).complete(lra);
    }
    
    @GET
    @Path(PARTICIPANT_PATH + "/status")
    @Produces(MediaType.TEXT_PLAIN)
    @Status
    public Response status(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader) throws MalformedURLException {
        URL lra = new URL(lraId);
        CompensatorStatus status = lraManagement.getParticipant(participantId, lra, null).getStatus(lra);
        
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(status.name()).build();
    } 
}
