package com.cashflow.common.audit.constants;

public final class AuditConstants {

    private AuditConstants() {
    }

    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String DEFAULT_UNKNOWN_SERVICE = "UNKNOWN_SERVICE";
    public static final String PLATFORM_TX_MANAGER = "platformTransactionManager";
}
