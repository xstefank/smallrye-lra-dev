package io.smallrye.lra.utils;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.time.temporal.ChronoUnit;

public class Utils {

    public static String extractLraId(URL lra) {
        return lra != null ? lra.toExternalForm().replaceFirst(".*/([^/?]+).*", "$1") : null;
    }

    public static String getFormattedString(URL parentLRA, String clientID, Long timeout, ChronoUnit unit) {
        return String.format("[parentLRA = %s, clientID = %s, timeout = %s, unit = %s]",
                parentLRA, clientID, timeout, unit);
    }

    public static boolean isInvalidResponse(Response response) {
        return response.getStatus() != Response.Status.OK.getStatusCode();
    }

    public static LRAStatus mapLRAStatus(String status) {
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
