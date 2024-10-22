package com.sparta.codechef.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class ExceptionCauseDto {
    private HttpStatus httpStatus;
    private Integer statusCode;
    private String message;
}
