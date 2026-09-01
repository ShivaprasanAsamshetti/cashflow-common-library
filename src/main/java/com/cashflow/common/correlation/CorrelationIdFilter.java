package com.cashflow.common.correlation;

import com.cashflow.common.constants.CorrelationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(CorrelationConstants.CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = CorrelationIdGenerator.generate();
        } else {
            correlationId = correlationId.trim();
        }

        MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, correlationId);

        // Recommended addition
        request.setAttribute(
                CorrelationConstants.CORRELATION_ID_MDC_KEY,
                correlationId
        );

        response.setHeader(
                CorrelationConstants.CORRELATION_ID_HEADER,
                correlationId
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        }
    }
}