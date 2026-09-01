package com.cashflow.common.config;

import com.cashflow.common.exception.SecurityErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CommonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CommonAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        Throwable jwtException = (Throwable) request.getAttribute("jwt_exception");
        String message;

        if (jwtException != null && jwtException.getMessage() != null) {
            message = jwtException.getMessage();
        } else if (authException != null && authException.getMessage() != null) {
            message = authException.getMessage();
        } else {
            message = "Full authentication is required to access this resource";
        }

        logger.warn("Unauthorized access attempt to URI: {} - Error: {}",
                request.getRequestURI(), message);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        SecurityErrorResponse errorDetails = SecurityErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message,
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
