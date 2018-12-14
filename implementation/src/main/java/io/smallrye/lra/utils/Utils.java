package io.smallrye.lra.utils;

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
}
