package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.TerminationException;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class SmallRyeLRAParticipant {
    
    private LRAParticipant delegate;
    private Map<String, LRAResult> associatedResults = new HashMap<>();

    public SmallRyeLRAParticipant(LRAParticipant delegate) {
        this.delegate = delegate;
    }

    public Response complete(URL lraId) {
        return endParticipantExecution(true, lraId);
    }

    public Response compensate(URL lraId) {
        return endParticipantExecution(false, lraId);
    }

    private Response endParticipantExecution(boolean complete, URL lraId) {
        Response response = null;
        Future<Void> result;

        try {
            if (complete) {
                result = delegate.completeWork(lraId);
            } else {
                result = delegate.compensateWork(lraId);
            }

            if (result == null) {
                response = Response.ok(complete ? CompensatorStatus.Completed : CompensatorStatus.Completing).build();
            } else {
                associatedResults.put(lraId.toExternalForm(), new LRAResult(result, complete));
            }
        } catch (TerminationException e) {
            response = Response.ok(complete ? CompensatorStatus.FailedToComplete : CompensatorStatus.FailedToCompensate).build();
        }

        return response;
    }
    
    private static final class LRAResult {
        private Future<Void> result;
        private boolean completing;

        public LRAResult(Future<Void> result, boolean completing) {
            this.result = result;
            this.completing = completing;
        }

        public Future<Void> getResult() {
            return result;
        }

        public boolean isCompleting() {
            return completing;
        }
    }

}
