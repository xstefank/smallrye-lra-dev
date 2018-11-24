package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.TerminationException;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

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
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) {
        return endParticipantExecution(false, lraId, participantId, data);
    }

    @PUT
    @Path(PARTICIPANT_PATH + "/complete")
    @Complete
    public Response complete(
            @PathParam(LRA_ID_PATH_PARAM) String lraId,
            @PathParam(PARTICIPANT_ID_PATH_PARAM) String participantId,
            @HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdHeader, String data) {
        return endParticipantExecution(true, lraId, participantId, data);
    }

    private Response endParticipantExecution(boolean complete, String lraId, String participantId, String data) {
        Response response = null;
        LRAParticipant participant = lraManagement.getParticipant(lraId, participantId);
        Future<Void> resultFuture;
        
        try {
            if (complete) {
                resultFuture = participant.completeWork(new URL(lraId));
            } else {
                resultFuture = participant.compensateWork(new URL(lraId));
            }
            
            if (resultFuture == null) {
                response = Response.ok(complete ? CompensatorStatus.Completed : CompensatorStatus.Completing).build();
            }
        } catch (MalformedURLException e) {
            response = Response.status(Response.Status.PRECONDITION_FAILED).entity(e).build();
        } catch (TerminationException e) {
            response = Response.ok(complete ? CompensatorStatus.FailedToComplete : CompensatorStatus.FailedToCompensate).build();
        }

        return response;
    }

}
