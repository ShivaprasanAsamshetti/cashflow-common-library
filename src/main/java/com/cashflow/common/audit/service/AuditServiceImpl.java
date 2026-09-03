package com.cashflow.common.audit.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cashflow.common.audit.constants.AuditConstants;
import com.cashflow.common.audit.entity.AuditLog;
import com.cashflow.common.audit.enums.AuditEventType;
import com.cashflow.common.audit.enums.AuditResult;
import com.cashflow.common.audit.model.AuditLogEvent;
import com.cashflow.common.audit.repository.AuditLogRepository;
import com.cashflow.common.constants.CorrelationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:UNKNOWN_SERVICE}")
    private String applicationName;

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(AuditLogEvent event) {
        if (event == null) {
            return;
        }
        try {
            String correlationId = event.getCorrelationId();
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = resolveCorrelationId();
            }

            String ipAddress = event.getIpAddress();
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = resolveIpAddress();
            }

            String service = event.getServiceName();
            if (service == null || service.isBlank()) {
                service = applicationName;
            }

            String finalMetadata = buildMetadataJson(service, event.getUserEmail(), event.getDescription(),
                    event.getMetadata(), event.getDetails());

            if (logger.isDebugEnabled()) {
                logger.debug("Recording audit event: service={} eventType={} result={} entityType={} entityId={} userId={} email={}",
                        service, event.getEventType(), event.getResult(), event.getEntityType(), event.getEntityId(),
                        event.getUserId(), event.getUserEmail());
            }

            AuditLog auditLog = AuditLog.builder()
                    .userId(event.getUserId())
                    .eventType(event.getEventType())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .result(event.getResult())
                    .ipAddress(ipAddress)
                    .correlationId(correlationId)
                    .metadata(finalMetadata)
                    .build();

            auditLogRepository.save(auditLog);
            logger.debug("Audit event persisted: service={} eventType={} result={} entityType={} entityId={}",
                    service, event.getEventType(), event.getResult(), event.getEntityType(), event.getEntityId());
        } catch (Exception ex) {
            logger.error("Failed to persist audit log: eventType={}, entityType={}, entityId={}. Error: {}",
                    event.getEventType(), event.getEntityType(), event.getEntityId(), ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(String eventType, String result, Long userId, String entityType, Long entityId) {
        recordEvent(eventType, result, userId, entityType, entityId, (String) null);
    }

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(String eventType, String result, Long userId, String entityType, Long entityId, String metadata) {
        AuditLogEvent event = AuditLogEvent.builder()
                .eventType(eventType)
                .result(result)
                .userId(userId)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .build();
        recordEvent(event);
    }

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId) {
        recordEvent(eventType != null ? eventType.name() : null, result != null ? result.name() : null, userId, entityType, entityId, (String) null);
    }

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId, String metadata) {
        recordEvent(eventType != null ? eventType.name() : null, result != null ? result.name() : null, userId, entityType, entityId, metadata);
    }

    @Override
    @Transactional(transactionManager = AuditConstants.PLATFORM_TX_MANAGER)
    public void recordEvent(AuditEventType eventType, AuditResult result, Long userId, String entityType, Long entityId, String userEmail, String metadata) {
        AuditLogEvent event = AuditLogEvent.builder()
                .eventType(eventType != null ? eventType.name() : null)
                .result(result != null ? result.name() : null)
                .userId(userId)
                .userEmail(userEmail)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .build();
        recordEvent(event);
    }

    private String buildMetadataJson(String serviceName, String userEmail, String description,
                                    String rawMetadata, Map<String, Object> details) {
        Map<String, Object> map = new HashMap<>();
        if (serviceName != null && !serviceName.isBlank()) {
            map.put("serviceName", serviceName);
        }
        if (userEmail != null && !userEmail.isBlank()) {
            map.put("userEmail", userEmail);
        }
        if (description != null && !description.isBlank()) {
            map.put("description", description);
        }
        if (details != null && !details.isEmpty()) {
            map.putAll(details);
        }

        if (rawMetadata != null && !rawMetadata.isBlank()) {
            try {
                Map<?, ?> parsed = objectMapper.readValue(rawMetadata, Map.class);
                parsed.forEach((k, v) -> map.put(String.valueOf(k), v));
            } catch (Exception ignored) {
                map.put("rawDetails", rawMetadata);
            }
        }

        if (map.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize audit metadata to JSON", e);
            return null;
        }
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = MDC.get("correlationId");
        }
        return correlationId;
    }

    private String resolveIpAddress() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String forwardedFor = request.getHeader(AuditConstants.FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
