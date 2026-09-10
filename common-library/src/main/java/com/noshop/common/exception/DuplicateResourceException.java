package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

/** Represents a resource creation or update that violates a uniqueness rule. */
public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String message) {
        super(message, ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT);
    }
}
