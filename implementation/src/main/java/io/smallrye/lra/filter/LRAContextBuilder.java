package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContextBuilder {

    private URL lraId;
    private boolean terminate;

    public LRAContextBuilder lraId(URL lraId) {
        this.lraId = lraId;
        return this;
    }

    public LRAContextBuilder terminate(boolean terminate) {
        this.terminate = terminate;
        return this;
    }

    public LRAContext build() {
        return new LRAContext(lraId, terminate);
    }
}
