package com.cashflow.common.constants;

public final class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static final String JWT_ERROR_ATTRIBUTE = "jwt.error";
    public static final String JWT_ERROR_EXPIRED = "expired";
    public static final String JWT_ERROR_INVALID = "invalid";

    private SecurityConstants() {
    }
}
