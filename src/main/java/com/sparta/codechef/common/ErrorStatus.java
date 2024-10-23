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
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 400, "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야 하며, 최소 8글자 이상이어야 합니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 400, "중복된 Email 입니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,404,"댓글을 찾지 못했습니다"),
    NOT_FOUND_COMMENT_LIST(HttpStatus.NOT_FOUND,404,"댓글 리스트를 찾지 못했습니다"),
    USER_DELETED(HttpStatus.BAD_REQUEST,400,"탈퇴된 유저입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,400,"잘못된 비밀번호 입니다."),
    ALREADY_ATTEND(HttpStatus.FORBIDDEN,403,"이미 출석한 유저입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,400,"유저를 찾지 못했습니다.");




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
