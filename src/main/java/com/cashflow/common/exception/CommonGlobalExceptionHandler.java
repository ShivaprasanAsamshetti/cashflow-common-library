package com.cashflow.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class CommonGlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommonGlobalExceptionHandler.class);

    /**
     * Handles all custom domain/business exceptions extending BaseCustomException
     * (e.g., BusinessException, ResourceNotFoundException, ResourceAlreadyExistsException,
     * or any custom exception created by teammates).
     */
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleBaseCustomException(
            BaseCustomException ex, HttpServletRequest request) {
        logger.warn("Custom business exception [{}]: {} on path [{}]",
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Handles Spring @Valid / @Validated DTO validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("DTO validation failed on path [{}]: {} error(s)", request.getRequestURI(),
                ex.getBindingResult().getErrorCount());

        List<CommonGlobalErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> CommonGlobalErrorResponse.FieldErrorDetail.builder()
                        .field(err.getField())
                        .rejectedValue(err.getRejectedValue())
                        .message(err.getDefaultMessage())
                        .build())
                .toList();

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("ERR_VALIDATION_FAILED")
                .message("Input validation failed. Please check the 'fieldErrors' details.")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles path/query constraint validation errors.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("Constraint violation on path [{}]: {}", request.getRequestURI(), ex.getMessage());

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("ERR_CONSTRAINT_VIOLATION")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles malformed JSON payloads.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Malformed HTTP request body on path [{}]: {}", request.getRequestURI(), ex.getMessage());

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("ERR_MALFORMED_JSON")
                .message("Required request body is missing or malformed JSON")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles invalid query/path method argument type mismatch.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = String.format("Parameter '%s' should be of type '%s'", ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        logger.warn("Method argument type mismatch on path [{}]: {}", request.getRequestURI(), msg);

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("ERR_INVALID_TYPE_MISMATCH")
                .message(msg)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("HTTP method '{}' not supported for path [{}]", ex.getMethod(), request.getRequestURI());

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .errorCode("ERR_METHOD_NOT_ALLOWED")
                .message(String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Catch-all fallback for any unhandled unexpected internal exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonGlobalErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unhandled unexpected exception on path [{}]:", request.getRequestURI(), ex);

        CommonGlobalErrorResponse response = CommonGlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .errorCode("ERR_INTERNAL_SERVER_ERROR")
                .message("An unexpected internal error occurred. Please try again later or contact support.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
