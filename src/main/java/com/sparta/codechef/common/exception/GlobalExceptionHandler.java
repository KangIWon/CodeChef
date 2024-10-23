package com.sparta.codechef.common.exception;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.ExceptionCauseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handleApiException(ApiException ex) {
        ExceptionCauseDto status = ex.getErrorCode().getCauseHttpStatus();
        return getErrorResponse(status.getHttpStatus(), status.getMessage());
    }

    public ResponseEntity<ApiResponse<String>> getErrorResponse(HttpStatus status, String message) {

        return new ResponseEntity<>(ApiResponse.createError( status.value(), message), status);
    }
}
