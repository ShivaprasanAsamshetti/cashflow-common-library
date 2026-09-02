package com.cashflow.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonSecurityExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(CommonSecurityExceptionHandler.class);

    // Unauthorized exception thrown from controller/service
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<SecurityErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        logger.warn("Unauthorized request on URI [{}]: {}",
                request.getRequestURI(), ex.getMessage());

        SecurityErrorResponse error = SecurityErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // User is authenticated but doesn't have permission
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SecurityErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        logger.warn("Access denied on URI [{}]: {}",
                request.getRequestURI(), ex.getMessage());

        SecurityErrorResponse error = SecurityErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access Denied: You do not have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}