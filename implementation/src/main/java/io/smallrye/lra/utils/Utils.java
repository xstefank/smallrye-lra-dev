package io.smallrye.lra.utils;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String extractLraId(URL lra) {
        return lra != null ? lra.getPath().replaceFirst(".*/([^/?]+).*", "$1") : null;
    }

    public static String getFormattedString(URL parentLRA, String clientID, Long timeout, TimeUnit unit) {
        return String.format("[parentLRA = %s, clientID = %s, timeout = %s, unit = %s]",
                parentLRA, clientID, timeout, unit);
    }

    public static boolean hasStatus(int actual, Response.Status... expected) {
        return Arrays.stream(expected).anyMatch(i -> i.getStatusCode() == actual);
    }
}
