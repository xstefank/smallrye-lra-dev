package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContext {

    public static final String CONTEXT_PROPERTY_NAME = "LRAContext";

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;

    public LRAContext(URL lraId, boolean newlyStarted, URL suspendedLRA) {
        this.lraId = lraId;
        this.newlyStarted = newlyStarted;
        this.suspendedLRA = suspendedLRA;
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
}
