package com.noshop.common.enums;

public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR,
    BAD_REQUEST,
    VALIDATION_ERROR,

    // Authentication
    UNAUTHORIZED,
    FORBIDDEN,
    INVALID_TOKEN,

    // Resource
    RESOURCE_NOT_FOUND,

    // Business
    DUPLICATE_RESOURCE,
    OPERATION_NOT_ALLOWED

}