package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException{
    public UnauthorizedException(String message) {
        super(message,ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }


}
