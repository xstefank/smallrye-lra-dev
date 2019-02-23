package io.smallrye.lra.participant;

import io.smallrye.lra.SmallRyeLRAClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class SmallRyeLRAParticipantService {
    
    private static final Map<String, SmallRyeRegisteredLRAParticipant> participants = new HashMap<>();
    
    @Inject
    private SmallRyeLRAClient lraClient;

    public void registerParticipant(Class<?> clazz, Map<String, Method> participantMethods) {
        participants.put(clazz.getName(), new SmallRyeRegisteredLRAParticipant(clazz, participantMethods));
    }

    public Response compensate(String participantId, String lraIdHeader) {
        try {
            participants.get(participantId).compensate(new URL(lraIdHeader));
        } catch (MalformedURLException e) {
            return Response.ok().build();
        }

        return Response.ok().build();
    }

    public Response complete(String participantId, String lraIdHeader) {
        try {
            participants.get(participantId).complete(new URL(lraIdHeader));
        } catch (MalformedURLException e) {
            return Response.ok().build();
        }

        return Response.ok().build();
    }
}
