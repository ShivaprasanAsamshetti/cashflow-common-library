package com.cashflow.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseCustomException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "ERR_RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND, "ERR_RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, HttpStatus.NOT_FOUND, errorCode);
    }
}
