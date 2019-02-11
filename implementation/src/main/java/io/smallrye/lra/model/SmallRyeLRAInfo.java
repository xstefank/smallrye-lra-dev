package io.smallrye.lra.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

public class SmallRyeLRAInfo {

    private String lraId;
    private long startTime;
    private long finishTime;
    private String clientId;
    private String status;
    private boolean complete;
    private boolean compensated;
    private boolean recovering;
    private boolean active;
    private boolean topLevel;
    
    public SmallRyeLRAInfo() {
    }

    public SmallRyeLRAInfo(String lraId, String clientId, String status, boolean isTopLevel) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.status = status;
        this.complete = status.equals(LRAStatus.Closed.name());
        this.compensated = status.equals(LRAStatus.Cancelled.name());
        this.recovering = status.equals(LRAStatus.FailedToClose.name()) || status.equals(LRAStatus.FailedToCancel.name());
        this.active = status.equals(LRAStatus.Closing.name()) || status.equals(LRAStatus.Cancelling.name());
        this.topLevel = isTopLevel;
    }

    public String getLraId() {
        return lraId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public String getClientId() {
        return clientId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isCompensated() {
        return compensated;
    }

    public boolean isRecovering() {
        return recovering;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        // Ignore
    }

}
