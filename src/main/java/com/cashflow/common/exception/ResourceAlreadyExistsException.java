package com.cashflow.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseCustomException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "ERR_RESOURCE_ALREADY_EXISTS");
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT, "ERR_RESOURCE_ALREADY_EXISTS");
    }

    public ResourceAlreadyExistsException(String message, String errorCode) {
        super(message, HttpStatus.CONFLICT, errorCode);
    }
}
