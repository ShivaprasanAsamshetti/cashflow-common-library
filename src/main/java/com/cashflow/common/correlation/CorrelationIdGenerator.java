package com.cashflow.common.correlation;

import java.util.UUID;

public final class CorrelationIdGenerator {

    private CorrelationIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
