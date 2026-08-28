package com.noshop.common.exception;

import com.noshop.common.enums.ErrorCode;
import com.noshop.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {

        ErrorResponse response = ErrorResponse.builder()
                                              .errorCode(ex.getErrorCode())
                                              .message(ex.getMessage())
                                              .status(ex.getHttpStatus()
                                                        .value())
                                              .build();

        return ResponseEntity.status(ex.getHttpStatus())
                             .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

//        maping the error
        for (FieldError error : exception.getBindingResult()
                                         .getFieldErrors()) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .errorCode(ErrorCode.VALIDATION_ERROR)
                                                   .message("Validation failed")
                                                   .status(HttpStatus.BAD_REQUEST.value())
                                                   .errors(errors)
                                                   .build();

        return ResponseEntity.badRequest()
                             .body(errorResponse);
    }


}