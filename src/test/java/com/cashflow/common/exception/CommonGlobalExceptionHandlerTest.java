package com.cashflow.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CommonGlobalExceptionHandlerTest {

    private CommonGlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new CommonGlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException("Invalid payment method", "ERR_PAYMENT_FAILED");
        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleBaseCustomException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_PAYMENT_FAILED", response.getBody().getErrorCode());
        assertEquals("Invalid payment method", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 101L);
        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleBaseCustomException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("User not found with id : '101'", response.getBody().getMessage());
    }

    @Test
    void testHandleResourceAlreadyExistsException() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("User", "email", "test@test.com");
        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleBaseCustomException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_RESOURCE_ALREADY_EXISTS", response.getBody().getErrorCode());
        assertEquals("User already exists with email : 'test@test.com'", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("userDto", "email", "invalid-email", false, null, null, "Email is invalid");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getErrorCount()).thenReturn(1);

        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_VALIDATION_FAILED", response.getBody().getErrorCode());
        assertEquals(1, response.getBody().getFieldErrors().size());
        assertEquals("email", response.getBody().getFieldErrors().get(0).getField());
    }

    @Test
    void testHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON");

        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_MALFORMED_JSON", response.getBody().getErrorCode());
    }

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = Mockito.mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Long.class);

        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_INVALID_TYPE_MISMATCH", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Parameter 'id' should be of type 'Long'"));
    }

    @Test
    void testHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_METHOD_NOT_ALLOWED", response.getBody().getErrorCode());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<CommonGlobalErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERR_INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }
}
