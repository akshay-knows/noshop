package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

/** Represents a valid request that violates a business-state rule. */
public class OperationNotAllowedException extends BaseException {

    public OperationNotAllowedException(String message) {
        super(message, ErrorCode.OPERATION_NOT_ALLOWED, HttpStatus.CONFLICT);
    }
}
