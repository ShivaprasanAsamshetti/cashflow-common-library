package com.cashflow.common.audit.service;

import com.cashflow.common.audit.enums.AuditEventType;
import com.cashflow.common.audit.enums.AuditResult;
import com.cashflow.common.audit.model.AuditLogEvent;

public interface AuditService {

    void recordEvent(AuditLogEvent event);

    void recordEvent(String eventType, String result, Long userId, String entityType, Long entityId);

    void recordEvent(String eventType, String result, Long userId, String entityType, Long entityId, String metadata);

    void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId);

    void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId, String metadata);

    void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId, String userEmail, String metadata);
}
