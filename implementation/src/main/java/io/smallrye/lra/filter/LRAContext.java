package io.smallrye.lra.filter;

import javax.ws.rs.core.Response;
import java.net.URL;

public class LRAContext {

    public static final String CONTEXT_PROPERTY_NAME = "LRAContext";

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;
    private Response.Status[] cancelOn;

    public LRAContext(URL lraId, boolean newlyStarted, URL suspendedLRA, Response.Status[] cancelOn) {
        this.lraId = lraId;
        this.newlyStarted = newlyStarted;
        this.suspendedLRA = suspendedLRA;
        this.cancelOn = cancelOn;
    }

    public URL getLraId() {
        return lraId;
    }

    public boolean isNewlyStarted() {
        return newlyStarted;
    }

    public URL getSuspendedLRA() {
        return suspendedLRA;
    }

    public Response.Status[] getCancelOn() {
        return cancelOn;
    }
}
