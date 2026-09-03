package com.cashflow.common.audit.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {

    private String eventType;
    private String result;
    private Long userId;
    private String userEmail;
    private String entityType;
    private Long entityId;
    private String description;
    private String metadata;
    private Map<String, Object> details;
    private String ipAddress;
    private String correlationId;
    private String serviceName;
}
