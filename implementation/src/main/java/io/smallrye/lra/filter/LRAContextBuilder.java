package io.smallrye.lra.filter;

import java.net.URL;

public class LRAContextBuilder {

    private URL lraId;

    public LRAContextBuilder lraId(URL lraId) {
        this.lraId = lraId;
        return this;
    }

    public LRAContext build() {
        return new LRAContext(lraId);
    }
}
