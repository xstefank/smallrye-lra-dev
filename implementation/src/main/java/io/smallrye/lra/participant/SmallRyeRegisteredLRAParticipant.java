package io.smallrye.lra.participant;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.participant.TerminationException;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Future;

public class SmallRyeRegisteredLRAParticipant {
    
    private Class<?> delegate;
    private Map<String, Method> participantMethods;

    public SmallRyeRegisteredLRAParticipant(Class<?> delegate, Map<String, Method> participantMethods) {
        this.delegate = delegate;
        this.participantMethods = participantMethods;
    }

//    public Response complete(URL lraId) {
//        return endParticipantExecution(lraId, true);
//    }

    public Response compensate(URL lraId) {
        System.out.println("COMPENSATE for " + delegate);
        Object bean = CDI.current().select(delegate).get();

        try {
            delegate.getMethod(participantMethods.get("compensate").getName()).invoke(bean, lraId, "test data");
        } catch (Exception e) {
            return Response.ok().build();
        }
        
        return Response.ok().build();
    }

    public Response complete(URL lraId) {
        System.out.println("COMPLETE for " + delegate);
        Object bean = CDI.current().select(delegate).get();

        try {
            delegate.getMethod(participantMethods.get("complete").getName()).invoke(bean, lraId, "test data");
        } catch (Exception e) {
            return Response.ok().build();
        }

        return Response.ok().build();
    }

//    public ParticipantStatus getStatus(URL lraId) {
//        LRAResult lraResult = associatedResults.get(lraId.toExternalForm());
//
//        if (lraResult == null) {
//            return null;
//        }
//
//        Future<Void> result = lraResult.getResult();
//
//        if (lraResult.isCompleting()) {
//            return getCompensatorStatusForResult(result, true);
//        } else {
//            return getCompensatorStatusForResult(result, false);
//        }

//    }
    private ParticipantStatus getCompensatorStatusForResult(Future<Void> result, boolean completing) {
        if (result.isDone()) {
            return completing ? ParticipantStatus.Completed : ParticipantStatus.Compensated;
        } else if (result.isCancelled()) {
            return completing ? ParticipantStatus.FailedToComplete : ParticipantStatus.FailedToCompensate;
        } else {
            return completing ? ParticipantStatus.Completing : ParticipantStatus.Compensating;
        }
    }

//    private Response endParticipantExecution(URL lraId, boolean completing) {
//        Response response = null;
//        Future<Void> result;
//
//        try {
//            if (completing) {
//                result = delegate.completeWork(lraId);
//            } else {
//                result = delegate.compensateWork(lraId);
//            }
//
//            if (result == null) {
//                response = Response.ok(completing ? ParticipantStatus.Completed.name() : ParticipantStatus.Completing.name()).build();
//            } else {
//                associatedResults.put(lraId.toExternalForm(), new LRAResult(result, completing));
//                response = Response.accepted(completing ? ParticipantStatus.Completing.name() : ParticipantStatus.Compensating.name()).build();
//            }
//        } catch (TerminationException | NotFoundException e) {
//            response = Response.ok(completing ? ParticipantStatus.FailedToComplete.name() : ParticipantStatus.FailedToCompensate.name()).build();
//        }
//
//        return response;
//    }
    
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
