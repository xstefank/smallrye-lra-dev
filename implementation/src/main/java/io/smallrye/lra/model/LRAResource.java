package io.smallrye.lra.model;

import java.net.URI;

public class LRAResource {
    
    private URI completeUri;
    private URI compensateUri;
    private URI statusUri;
    private URI forgetUri;
    private URI leaveUri;

    public LRAResource(URI completeUri, URI compensateUri, URI statusUri, URI forgetUri, URI leaveUri) {
        this.completeUri = completeUri;
        this.compensateUri = compensateUri;
        this.statusUri = statusUri;
        this.forgetUri = forgetUri;
        this.leaveUri = leaveUri;
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
                "completeUri=" + completeUri +
                ", compensateUri=" + compensateUri +
                ", statusUri=" + statusUri +
                ", forgetUri=" + forgetUri +
                ", leaveUri=" + leaveUri +
                '}';
    }

    public static final class LRAResourceBuilder {

        private URI completeUri;
        private URI compensateUri;
        private URI statusUri;
        private URI forgetUri;
        private URI leaveUri;

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
            return new LRAResource(completeUri, compensateUri, statusUri, forgetUri, leaveUri);
        }
        
    }
}
