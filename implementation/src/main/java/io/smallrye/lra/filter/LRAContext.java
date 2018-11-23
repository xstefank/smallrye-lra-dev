package io.smallrye.lra.filter;

import javax.ws.rs.core.Response;
import java.net.URL;

class LRAContext {

    static final String CONTEXT_PROPERTY_NAME = "LRAContext";

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;
    private Response.Status[] cancelOn;
    private Response.Status.Family[] cancelOnFamily;

    LRAContext(URL lraId, boolean newlyStarted, URL suspendedLRA, Response.Status[] cancelOn, Response.Status.Family[] cancelOnFamily) {
        this.lraId = lraId;
        this.newlyStarted = newlyStarted;
        this.suspendedLRA = suspendedLRA;
        this.cancelOn = cancelOn;
        this.cancelOnFamily = cancelOnFamily;
    }

    URL getLraId() {
        return lraId;
    }

    boolean isNewlyStarted() {
        return newlyStarted;
    }

    URL getSuspendedLRA() {
        return suspendedLRA;
    }

    Response.Status[] getCancelOn() {
        return cancelOn;
    }

    Response.Status.Family[] getCancelOnFamily() {
        return cancelOnFamily;
    }
}
