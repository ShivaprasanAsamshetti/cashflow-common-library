package com.cashflow.common.util;

import com.cashflow.common.constants.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;

public final class HeaderUtil {

    private HeaderUtil() {
    }

    public static String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return authHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    public static String extractHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }
}
