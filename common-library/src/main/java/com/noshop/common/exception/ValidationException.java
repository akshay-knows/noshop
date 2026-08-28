package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException{
    public ValidationException(String message) {
        super(message,ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
    }
}
