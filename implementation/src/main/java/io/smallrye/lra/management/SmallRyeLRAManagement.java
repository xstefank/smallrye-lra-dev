package io.smallrye.lra.management;

import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.participant.JoinLRAException;
import org.eclipse.microprofile.lra.participant.LRAManagement;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.LRAParticipantDeserializer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SmallRyeLRAManagement implements LRAManagement {
    
    private static final Map<String, SmallRyeLRAParticipant> participants = new HashMap<>();
    
    @Inject
    private LRAClient lraClient;

    @Context
    private UriInfo uriInfo;
    
    @Override
    public String joinLRA(LRAParticipant participant, URL lraId, Long timeLimit, TimeUnit unit) throws JoinLRAException {
        String recoveryUrl;
        
        try {
            recoveryUrl = lraClient.joinLRA(lraId, LRAParticipantResource.class, uriInfo.getBaseUri(), null);
            participants.put(recoveryUrl, new SmallRyeLRAParticipant(participant));
        } catch (GenericLRAException e) {
            throw new JoinLRAException(lraId, e.getStatusCode(), e.getMessage(), e);
        }
        
        return recoveryUrl;
    }

    @Override
    public String joinLRA(LRAParticipant participant, URL lraId) throws JoinLRAException {
        return joinLRA(participant, lraId, 0L, TimeUnit.SECONDS);
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
