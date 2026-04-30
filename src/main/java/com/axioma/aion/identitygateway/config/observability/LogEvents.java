package com.axioma.aion.identitygateway.config.observability;

public final class LogEvents {

    public static final String AUTHENTICATION_REQUEST = "AUTHENTICATION_REQUEST";
    public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
    public static final String SESSION_CREATE_REQUEST = "SESSION_CREATE_REQUEST";
    public static final String SESSION_CREATED = "SESSION_CREATED";
    public static final String SESSION_VALIDATE_REQUEST = "SESSION_VALIDATE_REQUEST";
    public static final String SESSION_VALIDATED = "SESSION_VALIDATED";
    public static final String SESSION_REVOKE_REQUEST = "SESSION_REVOKE_REQUEST";
    public static final String SESSION_REVOKED = "SESSION_REVOKED";
    public static final String ERROR = "ERROR";

    private LogEvents() {
    }
}
