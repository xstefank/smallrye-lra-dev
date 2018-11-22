package io.smallrye.lra.filter;

import javax.ws.rs.core.Response;
import java.net.URL;

public class LRAContextBuilder {

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;
    private Response.Status[] cancelOn;

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

    public LRAContextBuilder cancelOn(Response.Status[] cancelOn) {
        this.cancelOn = cancelOn;
        return this;
    }

    public LRAContext build() {
        return new LRAContext(lraId, newlyStarted, suspendedLRA, cancelOn);
    }
}
