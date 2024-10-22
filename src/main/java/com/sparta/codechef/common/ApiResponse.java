package com.sparta.codechef.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class ApiResponse<T> {

    private final Integer statusCode;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private ApiResponse(Integer statusCode, String message, T data) {
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

    public static <T> ApiResponse<T> onSuccess(T result) {
        return createSuccess( 200, "OK", result);
    }

    public static ApiResponse<String> onFailure(ErrorStatus errorStatus) {
        return new ApiResponse<>(errorStatus.getStatusCode(), errorStatus.getMessage(), null);
    }
}
