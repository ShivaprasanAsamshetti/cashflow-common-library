package com.cashflow.common.util;

import com.cashflow.common.constants.CorrelationConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static String getClientIpAddress() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        String clientIp = request.getHeader(CorrelationConstants.CLIENT_IP_HEADER);

        if (clientIp != null && !clientIp.isBlank()) {
            return clientIp.trim();
        }

        // Local development fallback
        return request.getRemoteAddr();
    }
}