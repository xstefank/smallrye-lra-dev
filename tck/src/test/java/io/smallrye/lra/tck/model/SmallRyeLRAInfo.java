package io.smallrye.lra.tck.model;

import io.smallrye.lra.model.SmallRyeLRAJSON;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.tck.LRAInfo;

public class SmallRyeLRAInfo implements LRAInfo {
    
    private final String lraId;
    private final String clientId;
    private final LRAStatus status;
    private final boolean topLevel;

    public SmallRyeLRAInfo(String lraId, String clientId, LRAStatus status, boolean topLevel) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.status = status;
        this.topLevel = topLevel;
    }

    public static SmallRyeLRAInfo of(SmallRyeLRAJSON json) {
        return new SmallRyeLRAInfo(json.getLraId(), json.getClientId(),
                mapLRAStatus(json.getStatus()), json.isTopLevel());
    }

    @Override
    public String getLraId() {
        return lraId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public boolean isComplete() {
        return status.equals(LRAStatus.Closed);
    }

    @Override
    public boolean isCompensated() {
        return status.equals(LRAStatus.Cancelled);
    }

    @Override
    public boolean isRecovering() {
        return status.equals(LRAStatus.Closing) || status.equals(LRAStatus.Cancelling);
    }

    @Override
    public boolean isActive() {
        return status.equals(LRAStatus.Active);
    }

    @Override
    public boolean isTopLevel() {
        return topLevel;
    }

    private static LRAStatus mapLRAStatus(String status) {
        if (status.equals("Compensating")) {
            return LRAStatus.Cancelling;
        } else if (status.equals("Compensated")) {
            return LRAStatus.Cancelled;
        } else if (status.equals("FailedToCompensate")) {
            return LRAStatus.FailedToCancel;
        } else if (status.equals("Completing")) {
            return LRAStatus.Closing;
        } else if (status.equals("Completed")) {
            return LRAStatus.Closed;
        } else if (status.equals("FailedToComplete")) {
            return LRAStatus.FailedToClose;
        } else {
            return LRAStatus.Active;
        }
    }
}
