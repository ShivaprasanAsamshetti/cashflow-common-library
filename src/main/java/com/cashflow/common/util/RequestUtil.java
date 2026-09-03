package com.cashflow.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cashflow.common.constants.CorrelationConstants;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private static final String UNKNOWN = "unknown";

    private static final String[] IP_HEADER_CANDIDATES = {
            CorrelationConstants.CLIENT_IP_HEADER, // "X-Client-IP"
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private RequestUtil() {
    }

    public static String getClientIpAddress() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        return getClientIpAddress(attributes.getRequest());
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isBlank() && !UNKNOWN.equalsIgnoreCase(ipList.trim())) {
                // In case of multiple proxies in header (e.g. "client, proxy1, proxy2"), the original client IP is the first one
                String[] ips = ipList.split(",");
                for (String ip : ips) {
                    String candidate = ip.trim();
                    if (!candidate.isBlank() && !UNKNOWN.equalsIgnoreCase(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        // Direct remote address fallback
        return request.getRemoteAddr();
    }
}