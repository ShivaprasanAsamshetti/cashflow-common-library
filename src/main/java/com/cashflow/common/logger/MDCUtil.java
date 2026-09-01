package com.cashflow.common.logger;

import com.cashflow.common.constants.CorrelationConstants;
import org.slf4j.MDC;

public final class MDCUtil {

    private MDCUtil() {
    }

    // Correlation ID
    public static void putCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, correlationId);
        }
    }

    public static String getCorrelationId() {
        return MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY);
    }

    public static void removeCorrelationId() {
        MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
    }

    // User Public ID
    public static void putUserPublicId(String publicId) {
        if (publicId != null && !publicId.isBlank()) {
            MDC.put(LogConstants.USER_PUBLIC_ID_KEY, publicId);
        }
    }

    // Masked User Email
    public static void putUserEmail(String email) {
        if (email != null && !email.isBlank()) {
            MDC.put(LogConstants.USER_EMAIL_KEY, email);
        }
    }

    // Remove any MDC key
    public static void remove(String key) {
        MDC.remove(key);
    }

    // Clear all MDC values at end of request
    public static void clear() {
        MDC.clear();
    }
}