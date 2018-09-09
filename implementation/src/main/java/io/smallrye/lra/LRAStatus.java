package io.smallrye.lra;

public enum LRAStatus {

    ACTIVE("active"),
    RECOVERING("recovering"),
    
    ;

    private String queryParam;
    
    LRAStatus(String queryParam) {
        this.queryParam = queryParam;
    }
}
