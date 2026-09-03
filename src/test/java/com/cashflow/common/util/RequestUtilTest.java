package com.cashflow.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cashflow.common.constants.CorrelationConstants;

class RequestUtilTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Should extract IP from X-Client-IP header when present")
    void getClientIpAddress_FromXClientIp() {
        request.addHeader(CorrelationConstants.CLIENT_IP_HEADER, "203.0.113.195");
        request.setRemoteAddr("127.0.0.1");

        String ip = RequestUtil.getClientIpAddress();
        assertEquals("203.0.113.195", ip);
    }

    @Test
    @DisplayName("Should extract first client IP from comma-separated X-Forwarded-For header")
    void getClientIpAddress_FromXForwardedFor() {
        request.addHeader("X-Forwarded-For", "198.51.100.42, 10.0.0.1, 172.16.0.1");

        String ip = RequestUtil.getClientIpAddress();
        assertEquals("198.51.100.42", ip);
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP header if higher priority headers absent")
    void getClientIpAddress_FromXRealIp() {
        request.addHeader("X-Real-IP", "192.0.2.1");

        String ip = RequestUtil.getClientIpAddress();
        assertEquals("192.0.2.1", ip);
    }

    @Test
    @DisplayName("Should fall back to getRemoteAddr when no proxy headers present")
    void getClientIpAddress_FallbackRemoteAddr() {
        request.setRemoteAddr("10.10.10.10");

        String ip = RequestUtil.getClientIpAddress();
        assertEquals("10.10.10.10", ip);
    }

    @Test
    @DisplayName("Should return null when RequestContextHolder attributes are null")
    void getClientIpAddress_NoRequestContext_ReturnsNull() {
        RequestContextHolder.resetRequestAttributes();

        String ip = RequestUtil.getClientIpAddress();
        assertNull(ip);
    }

    @Test
    @DisplayName("Should handle unknown and blank values gracefully in headers")
    void getClientIpAddress_SkipsUnknownAndBlanks() {
        request.addHeader(CorrelationConstants.CLIENT_IP_HEADER, "unknown");
        request.addHeader("X-Forwarded-For", "unknown, 198.51.100.99");

        String ip = RequestUtil.getClientIpAddress();
        assertEquals("198.51.100.99", ip);
    }
}
