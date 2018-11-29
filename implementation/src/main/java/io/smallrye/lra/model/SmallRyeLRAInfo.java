package io.smallrye.lra.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.LRAInfo;

public class SmallRyeLRAInfo implements LRAInfo {

    private String lraId;
    private int startTime;
    private int finishTime;
    private String clientId;
    private CompensatorStatus status;
    private boolean complete;
    private boolean compensated;
    private boolean recovering;
    private boolean active;
    private boolean topLevel;
    
    public SmallRyeLRAInfo() {
    }

    public SmallRyeLRAInfo(String lraId, String clientId, CompensatorStatus status, boolean isTopLevel) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.status = status;
        this.complete = status == CompensatorStatus.Completed;
        this.compensated = status == CompensatorStatus.Compensated;
        this.recovering = status == CompensatorStatus.FailedToComplete || status == CompensatorStatus.FailedToCompensate;
        this.active = status == CompensatorStatus.Completing || status == CompensatorStatus.Compensating;
        this.topLevel = isTopLevel;
    }

    @Override
    public String getLraId() {
        return lraId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public CompensatorStatus getStatus() {
        return status;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public boolean isCompensated() {
        return compensated;
    }

    @Override
    public boolean isRecovering() {
        return recovering;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isTopLevel() {
        return topLevel;
    }

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        // Ignore
    }

}
