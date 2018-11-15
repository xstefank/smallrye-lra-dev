package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContext {

    public static final String CONTEXT_PROPERTY_NAME = "LRAContext";

    private URL lraId;

    public LRAContext(URL lraId) {
        this.lraId = lraId;
    }

    public URL getLraId() {
        return lraId;
    }

}
