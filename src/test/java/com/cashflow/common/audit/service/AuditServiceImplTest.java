package com.cashflow.common.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cashflow.common.audit.constants.AuditConstants;
import com.cashflow.common.audit.entity.AuditLog;
import com.cashflow.common.audit.enums.AuditEventType;
import com.cashflow.common.audit.enums.AuditResult;
import com.cashflow.common.audit.model.AuditLogEvent;
import com.cashflow.common.audit.repository.AuditLogRepository;
import com.cashflow.common.constants.CorrelationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditServiceImpl auditService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auditService, "applicationName", "TEST-SERVICE");
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    @DisplayName("Should record audit event using AuditLogEvent model successfully")
    void recordEvent_WithAuditLogEvent_Success() {
        MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, "corr-12345");
        request.setRemoteAddr("192.168.1.100");

        AuditLogEvent event = AuditLogEvent.builder()
                .eventType("LOGIN_SUCCESS")
                .result("SUCCESS")
                .userId(101L)
                .userEmail("test@example.com")
                .entityType("USER")
                .entityId(101L)
                .description("User logged in")
                .details(Map.of("role", "ROLE_USER"))
                .build();

        auditService.recordEvent(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(101L, saved.getUserId());
        assertEquals("LOGIN_SUCCESS", saved.getEventType());
        assertEquals("SUCCESS", saved.getResult());
        assertEquals("USER", saved.getEntityType());
        assertEquals(101L, saved.getEntityId());
        assertEquals("192.168.1.100", saved.getIpAddress());
        assertEquals("corr-12345", saved.getCorrelationId());
        assertNotNull(saved.getMetadata());
        assertTrue(saved.getMetadata().contains("TEST-SERVICE"));
        assertTrue(saved.getMetadata().contains("test@example.com"));
        assertTrue(saved.getMetadata().contains("User logged in"));
    }

    @Test
    @DisplayName("Should record audit event using enum overload")
    void recordEvent_WithEnums_Success() {
        request.addHeader(AuditConstants.FORWARDED_FOR_HEADER, "10.0.0.1, 192.168.1.1");

        auditService.recordEvent(AuditEventType.PROFILE_CREATED, AuditResult.SUCCESS, 202L, "USER_PROFILE", 500L);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(202L, saved.getUserId());
        assertEquals("PROFILE_CREATED", saved.getEventType());
        assertEquals("SUCCESS", saved.getResult());
        assertEquals("USER_PROFILE", saved.getEntityType());
        assertEquals(500L, saved.getEntityId());
        assertEquals("10.0.0.1", saved.getIpAddress());
    }

    @Test
    @DisplayName("Should record audit event with string overload and custom metadata")
    void recordEvent_WithStringAndMetadata_Success() {
        auditService.recordEvent("CUSTOM_EVENT", "FAILURE", 303L, "ACCOUNT", 700L, "{\"reason\":\"invalid_input\"}");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(303L, saved.getUserId());
        assertEquals("CUSTOM_EVENT", saved.getEventType());
        assertEquals("FAILURE", saved.getResult());
        assertTrue(saved.getMetadata().contains("invalid_input"));
        assertTrue(saved.getMetadata().contains("TEST-SERVICE"));
    }

    @Test
    @DisplayName("Should not throw exception if repository throws an error")
    void recordEvent_RepositoryFails_DoesNotPropagateException() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        AuditLogEvent event = AuditLogEvent.builder()
                .eventType("FAIL_EVENT")
                .build();

        // Should not throw
        auditService.recordEvent(event);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should handle null event safely")
    void recordEvent_NullEvent_DoesNothing() {
        auditService.recordEvent((AuditLogEvent) null);
        verify(auditLogRepository, never()).save(any());
    }
}
