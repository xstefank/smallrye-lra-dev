package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContext {

    public static final String CONTEXT_PROPERTY_NAME = "LRAContext";

    private URL lraId;
    private boolean terminate;

    public LRAContext(URL lraId, boolean terminate) {
        this.lraId = lraId;
        this.terminate = terminate;
    }

    public URL getLraId() {
        return lraId;
    }

    public boolean shouldTerminate() {
        return terminate;
    }
}
