package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.TerminationException;

import javax.ws.rs.NotFoundException;
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
        return endParticipantExecution(lraId, true);
    }

    public Response compensate(URL lraId) {
        return endParticipantExecution(lraId, false);
    }

    public ParticipantStatus getStatus(URL lraId) {
        LRAResult lraResult = associatedResults.get(lraId.toExternalForm());

        if (lraResult == null) {
            return null;
        }

        Future<Void> result = lraResult.getResult();

        if (lraResult.isCompleting()) {
            return getCompensatorStatusForResult(result, true);
        } else {
            return getCompensatorStatusForResult(result, false);
        }
    }

    private ParticipantStatus getCompensatorStatusForResult(Future<Void> result, boolean completing) {
        if (result.isDone()) {
            return completing ? ParticipantStatus.Completed : ParticipantStatus.Compensated;
        } else if (result.isCancelled()) {
            return completing ? ParticipantStatus.FailedToComplete : ParticipantStatus.FailedToCompensate;
        } else {
            return completing ? ParticipantStatus.Completing : ParticipantStatus.Compensating;
        }
    }

    private Response endParticipantExecution(URL lraId, boolean completing) {
        Response response = null;
        Future<Void> result;

        try {
            if (completing) {
                result = delegate.completeWork(lraId);
            } else {
                result = delegate.compensateWork(lraId);
            }

            if (result == null) {
                response = Response.ok(completing ? ParticipantStatus.Completed.name() : ParticipantStatus.Completing.name()).build();
            } else {
                associatedResults.put(lraId.toExternalForm(), new LRAResult(result, completing));
                response = Response.accepted(completing ? ParticipantStatus.Completing.name() : ParticipantStatus.Compensating.name()).build();
            }
        } catch (TerminationException | NotFoundException e) {
            response = Response.ok(completing ? ParticipantStatus.FailedToComplete.name() : ParticipantStatus.FailedToCompensate.name()).build();
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
