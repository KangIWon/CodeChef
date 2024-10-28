package com.sparta.codechef.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.ExceptionCause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handleApiException(ApiException ex) {
        ExceptionCause status = ex.getErrorCode().getCauseHttpStatus();
        return getErrorResponse(status.getHttpStatus(), status.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<String>> handleSQLException(SQLException ex) {
        log.error(ex.getMessage());
        throw new ApiException(ErrorStatus.SQL_EXCEPTION_OCCURRED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) throws JsonProcessingException {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(
                    error -> errorMap.put(error.getField(), error.getDefaultMessage())
                );

        String errorMessage = new ObjectMapper().writeValueAsString(errorMap);

        return getErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    public ResponseEntity<ApiResponse<String>> getErrorResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(ApiResponse.createError( status.value(), message), status);
    }
}