package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException {

    public EmailAlreadyExistsException(String message) {
        super(message, ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT);
    }

}