package io.smallrye.lra.filter;

import javax.ws.rs.core.Response;
import java.net.URL;

class LRAContextBuilder {

    private URL lraId;
    private boolean newlyStarted;
    private URL suspendedLRA;
    private Response.Status[] cancelOn;
    private Response.Status.Family[] cancelOnFamily;

    LRAContextBuilder lraId(URL lraId) {
        this.lraId = lraId;
        return this;
    }

    LRAContextBuilder newlyStarted(boolean newlyStarted) {
        this.newlyStarted = newlyStarted;
        return this;
    }

    LRAContextBuilder suspend(URL lraId) {
        this.suspendedLRA = lraId;
        return this;
    }

    LRAContextBuilder cancelOn(Response.Status[] cancelOn) {
        this.cancelOn = cancelOn;
        return this;
    }

    LRAContextBuilder cancelOnFamily(Response.Status.Family[] cancelOnFamily) {
        this.cancelOnFamily = cancelOnFamily;
        return this;
    }
    
    LRAContext build() {
        return new LRAContext(lraId, newlyStarted, suspendedLRA, cancelOn, cancelOnFamily);
    }
}
