package com.cashflow.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseCustomException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "ERR_BUSINESS_VALIDATION");
    }

    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
