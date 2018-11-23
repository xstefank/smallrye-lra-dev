package io.smallrye.lra.utils;

public final class LRAConstants {

    private LRAConstants() {}

    public static final long DEFAULT_TIMELIMIT = 0L;

    public static final String CLIENT_ID = "ClientId";
    public static final String PARENT_LRA = "ParentLRA";
    public static final String TIMELIMIT = "TimeLimit";
    public static final String LRA_ID_PATH_PARAM = "LraId";
    public static final String RECOVERY_ID_PATH_PARAM = "RecoveryId";
    public static final String STATUS = "Status";

    public static final String START = "/start";
    public static final String CLOSE = String.format("/{%s}/close", LRA_ID_PATH_PARAM);
    public static final String CANCEL = String.format("/{%s}/cancel", LRA_ID_PATH_PARAM);


    public static final String UTF_8 = "UTF-8";
}
