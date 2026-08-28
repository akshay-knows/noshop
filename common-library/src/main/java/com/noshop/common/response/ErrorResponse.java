package com.noshop.common.response;

import com.noshop.common.enums.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;


@Getter
@Builder
public class ErrorResponse {
    @Builder.Default
    private final boolean success = false;
    private final ErrorCode errorCode;
    private final String message;
    private final int status;

    private final Map<String, String> errors;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}
