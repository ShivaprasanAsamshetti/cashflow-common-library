package com.cashflow.common.constants;

public final class CorrelationConstants {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String CLIENT_IP_HEADER = "X-Client-IP";
    private CorrelationConstants() {
    }
}
