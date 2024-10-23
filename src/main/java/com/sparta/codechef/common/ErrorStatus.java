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

    // 유저 관련 예외
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 유저입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "유저를 찾을 수 없습니다."),

    // 댓글 관련 예외
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,404,"댓글을 찾지 못했습니다"),
    NOT_FOUND_COMMENT_LIST(HttpStatus.NOT_FOUND,404,"댓글 리스트를 찾지 못했습니다"),
    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, 400, "ErrorStatus 예시"),

    // 빌더 관련 예외
    ID_CANNOT_BE_SET(HttpStatus.BAD_REQUEST, 400, "ID 필드는 설정할 수 없습니다."),
    CREATED_AT_CANNOT_BE_SET(HttpStatus.BAD_REQUEST, 400, "createdAt 필드는 설정할 수 없습니다."),

    // 채팅방 관련 예외
    BAD_REQUEST_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, 400, "채팅방 정원은 최소 2명, 최대 100명입니다."),
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, 404, "채팅방을 찾을 수 없습니다."),
    ALREADY_IN_CHATROOM(HttpStatus.CONFLICT, 409, "이미 채팅방에 접속해 있습니다."),
    ROOM_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, 409, "채팅방 정원이 초과되었습니다."),
    NOT_IN_CHATROOM(HttpStatus.CONFLICT, 409, "현재 채팅방에 접속해 있지 않습니다.");




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
