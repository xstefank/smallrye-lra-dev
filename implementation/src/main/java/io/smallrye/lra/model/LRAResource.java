package io.smallrye.lra.model;

import java.net.URI;

public class LRAResource {
    
    private URI lraUri;
    private URI completeUri;
    private URI compensateUri;
    private URI statusUri;
    private URI forgetUri;
    private URI leaveUri;

    public LRAResource(URI lraUri, URI completeUri, URI compensateUri, URI statusUri, URI forgetUri, URI leaveUri) {
        this.lraUri = lraUri;
        this.completeUri = completeUri;
        this.compensateUri = compensateUri;
        this.statusUri = statusUri;
        this.forgetUri = forgetUri;
        this.leaveUri = leaveUri;
    }

    public URI getLraUri() {
        return lraUri;
    }

    public URI getCompleteUri() {
        return completeUri;
    }

    public URI getCompensateUri() {
        return compensateUri;
    }

    public URI getStatusUri() {
        return statusUri;
    }

    public URI getForgetUri() {
        return forgetUri;
    }

    public URI getLeaveUri() {
        return leaveUri;
    }

    public static LRAResourceBuilder builder() {
        return new LRAResourceBuilder();
    }

    @Override
    public String toString() {
        return "LRAResource{" +
                "lraUri=" + lraUri +
                ", completeUri=" + completeUri +
                ", compensateUri=" + compensateUri +
                ", statusUri=" + statusUri +
                ", forgetUri=" + forgetUri +
                ", leaveUri=" + leaveUri +
                '}';
    }

    public static final class LRAResourceBuilder {

        private URI lraUri;
        private URI completeUri;
        private URI compensateUri;
        private URI statusUri;
        private URI forgetUri;
        private URI leaveUri;

        public LRAResourceBuilder lraUri(URI lraUri) {
            this.lraUri = lraUri;
            return this;
        }

        public LRAResourceBuilder completeUri(URI completeUri) {
            this.completeUri = completeUri;
            return this;
        }

        public LRAResourceBuilder compensateUri(URI compensateUri) {
            this.compensateUri = compensateUri;
            return this;
        }

        public LRAResourceBuilder statusUri(URI statusUri) {
            this.statusUri = statusUri;
            return this;
        }

        public LRAResourceBuilder forgetUri(URI forgetUri) {
            this.forgetUri = forgetUri;
            return this;
        }

        public LRAResourceBuilder leaveUri(URI leaveUri) {
            this.leaveUri = leaveUri;
            return this;
        }
        
        public LRAResource build() {
            return new LRAResource(lraUri, completeUri, compensateUri, statusUri, forgetUri, leaveUri);
        }
        
    }
}
