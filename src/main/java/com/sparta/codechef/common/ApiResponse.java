package com.sparta.codechef.common;

import com.sparta.codechef.domain.chat.entity.Message;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private final Integer statusCode;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(Integer statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> createSuccess(Integer statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }

    public static <T> ApiResponse<T> createError(Integer statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return createSuccess(HttpStatus.OK.value(), message, result);
    }

    public static <T> ApiResponse<T> onSuccess(String message, T result) {
        return new ApiResponse<>(200, message, result);
    }
}
