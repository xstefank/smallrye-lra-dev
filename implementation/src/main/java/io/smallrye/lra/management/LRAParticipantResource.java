package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
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
    @Compensate
    public Response compensate(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) throws MalformedURLException {
        return lraManagement.getParticipant(participantId).compensate(new URL(lraId));
    }

    @PUT
    @Path(PARTICIPANT_PATH + "/complete")
    @Complete
    public Response complete(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) throws MalformedURLException {
        return lraManagement.getParticipant(participantId).complete(new URL(lraId));
    }
    
    @GET
    @Path(PARTICIPANT_PATH + "/status")
    @Status
    public Response status(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader) throws MalformedURLException {
        CompensatorStatus status = lraManagement.getParticipant(participantId).getStatus(new URL(lraId));
        
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(status.name()).build();
    } 
}
