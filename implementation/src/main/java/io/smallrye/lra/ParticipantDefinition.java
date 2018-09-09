package io.smallrye.lra;

import java.net.URL;

public class ParticipantDefinition {
    
    private URL completeURL;
    private URL compensateURL;
    private URL forgetURL;
    private URL leaveURL;
    private URL statusURL;
    private String compensatorData;

    public ParticipantDefinition(URL completeURL, URL compensateURL, URL forgetURL, URL leaveURL, URL statusURL, String compensatorData) {
        this.completeURL = completeURL;
        this.compensateURL = compensateURL;
        this.forgetURL = forgetURL;
        this.leaveURL = leaveURL;
        this.statusURL = statusURL;
        this.compensatorData = compensatorData;
    }

    public URL getCompleteURL() {
        return completeURL;
    }

    public void setCompleteURL(URL completeURL) {
        this.completeURL = completeURL;
    }

    public URL getCompensateURL() {
        return compensateURL;
    }

    public void setCompensateURL(URL compensateURL) {
        this.compensateURL = compensateURL;
    }

    public URL getForgetURL() {
        return forgetURL;
    }

    public void setForgetURL(URL forgetURL) {
        this.forgetURL = forgetURL;
    }

    public URL getLeaveURL() {
        return leaveURL;
    }

    public void setLeaveURL(URL leaveURL) {
        this.leaveURL = leaveURL;
    }

    public URL getStatusURL() {
        return statusURL;
    }

    public void setStatusURL(URL statusURL) {
        this.statusURL = statusURL;
    }

    public String getCompensatorData() {
        return compensatorData;
    }

    public void setCompensatorData(String compensatorData) {
        this.compensatorData = compensatorData;
    }
}
