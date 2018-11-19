package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContextBuilder {

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;

    public LRAContextBuilder lraId(URL lraId) {
        this.lraId = lraId;
        return this;
    }

    public LRAContextBuilder newlyStarted(boolean newlyStarted) {
        this.newlyStarted = newlyStarted;
        return this;
    }

    public LRAContextBuilder suspend(URL lraId) {
        this.suspendedLRA = lraId;
        return this;
    }

    public LRAContext build() {
        return new LRAContext(lraId, newlyStarted, suspendedLRA);
    }
}
