package io.smallrye.lra.model;

public class LRAResource {
    
    private String lraUrl;
    private String completeUrl;
    private String compensateUrl;
    private String statusUrl;
    private String forgetUrl;
    private String leaveUrl;

    public LRAResource(String lraUrl, String completeUrl, String compensateUrl, String statusUrl, String forgetUrl, String leaveUrl) {
        this.lraUrl = lraUrl;
        this.completeUrl = completeUrl;
        this.compensateUrl = compensateUrl;
        this.statusUrl = statusUrl;
        this.forgetUrl = forgetUrl;
        this.leaveUrl = leaveUrl;
    }

    public String getLraUrl() {
        return lraUrl;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    public String getCompensateUrl() {
        return compensateUrl;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public String getForgetUrl() {
        return forgetUrl;
    }

    public String getLeaveUrl() {
        return leaveUrl;
    }
    
    public static LRAResourceBuilder builder() {
        return new LRAResourceBuilder();
    }
    
    public static final class LRAResourceBuilder {

        private String lraUrl;
        private String completeUrl;
        private String compensateUrl;
        private String statusUrl;
        private String forgetUrl;
        private String leaveUrl;

        public LRAResourceBuilder lraUrl(String lraUrl) {
            this.lraUrl = lraUrl;
            return this;
        }

        public LRAResourceBuilder completeUrl(String completeUrl) {
            this.completeUrl = completeUrl;
            return this;
        }

        public LRAResourceBuilder compensateUrl(String compensateUrl) {
            this.compensateUrl = compensateUrl;
            return this;
        }

        public LRAResourceBuilder statusUrl(String statusUrl) {
            this.statusUrl = statusUrl;
            return this;
        }

        public LRAResourceBuilder forgetUrl(String forgetUrl) {
            this.forgetUrl = forgetUrl;
            return this;
        }

        public LRAResourceBuilder leaveUrl(String leaveUrl) {
            this.leaveUrl = leaveUrl;
            return this;
        }
        
        public LRAResource build() {
            return new LRAResource(lraUrl, completeUrl, compensateUrl, statusUrl, forgetUrl, leaveUrl);
        }
        
    }
}
