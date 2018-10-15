package io.smallrye.lra.utils;

public final class LRAConstants {
    
    private LRAConstants() {}

    public static final String COORDINATOR = "/lra-coordinator";

    public static final String CLIENT_ID_PARAM = "ClientID";
    public static final String TIMELIMIT_PARAM = "TimeLimit";
    public static final String PARENT_LRA_PARAM = "ParentLRA";
    public static final String LRA_ID_PATH_PARAM = "LraId";
    public static final String RECOVERY_ID_PATH_NAME = "RecoveryId";
    public static final String STATUS = "status";
    public static final String TIMELIMIT = "TimeLimit";
    
    public static final String START = "/start";
    public static final String CLOSE = String.format("/{%s}/close", LRA_ID_PATH_PARAM);
    public static final String CANCEL = String.format("/{%s}/cancel", LRA_ID_PATH_PARAM);


    public static final String UTF_8 = "UTF-8";
}
