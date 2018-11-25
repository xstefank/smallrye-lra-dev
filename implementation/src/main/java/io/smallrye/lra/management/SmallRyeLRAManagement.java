package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.participant.JoinLRAException;
import org.eclipse.microprofile.lra.participant.LRAManagement;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.LRAParticipantDeserializer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SmallRyeLRAManagement implements LRAManagement {
    
    private static final Map<String, SmallRyeLRAParticipant> participants = new HashMap<>();
    
    @Inject
    private LRAClient lraClient;
    
    @Override
    public String joinLRA(LRAParticipant participant, URL lraId, Long timeLimit, TimeUnit unit) throws JoinLRAException {
//        lraClient.joinLRA(lraId, )
        return null;
    }

    @Override
    public String joinLRA(LRAParticipant participant, URL lraId) throws JoinLRAException {
        return null;
    }

    @Override
    public void registerDeserializer(LRAParticipantDeserializer deserializer) {

    }

    @Override
    public void unregisterDeserializer(LRAParticipantDeserializer deserializer) {

    }

    public SmallRyeLRAParticipant getParticipant(String participantId) {
        return participants.get(participantId);
    }
}
