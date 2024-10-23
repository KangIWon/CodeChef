package com.sparta.codechef.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {
    //예외 예시
    BAD_REQUEST_UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST,400,"지원되지 않는 JWT 토큰입니다."),
    BAD_REQUEST_ILLEGAL_TOKEN(HttpStatus.BAD_REQUEST,400,"잘못된 JWT 토큰입니다."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED,401,"유효하지 않는 JWT 서명입니다."),
    UNAUTHORIZED_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,401,"만료된 JWT 토큰입니다."),
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED,401,"JWT 토큰 검증 중 오류가 발생했습니다."),
    FORBIDDEN_TOKEN(HttpStatus.FORBIDDEN, 403, "관리자 권한이 없습니다."),
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, 404, "JWT 토큰이 필요합니다."),
    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, 400, "ApiException 예외 처리 예시"),
    NOT_FOUND_USER(HttpStatus.BAD_REQUEST,400,"해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_BOARD(HttpStatus.BAD_REQUEST,400,"해당 게시물을 찾을 수 없습니다."),
    NOT_THE_AUTHOR(HttpStatus.NOT_ACCEPTABLE, 406, "게시물 작성자가 아닙니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,404,"댓글을 찾지 못했습니다"),
    NOT_FOUND_COMMENT_LIST(HttpStatus.NOT_FOUND,404,"댓글 리스트를 찾지 못했습니다");




    private final HttpStatus httpStatus;
    private final Integer statusCode;
    private final String message;

    @Override
    public ExceptionCause getCauseHttpStatus() {
        return ExceptionCause.builder()
                .httpStatus(httpStatus)
                .statusCode(statusCode)
                .message(message)
                .build();
    }
}
