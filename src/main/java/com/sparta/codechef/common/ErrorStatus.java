package com.sparta.codechef.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {
    //예외 예시
    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, 400, "ApiException 예외 처리 예시"),

    // Security 관련 예외
    BAD_REQUEST_UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST,400,"지원되지 않는 JWT 토큰입니다."),
    BAD_REQUEST_ILLEGAL_TOKEN(HttpStatus.BAD_REQUEST,400,"잘못된 JWT 토큰입니다."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED,401,"유효하지 않는 JWT 서명입니다."),
    UNAUTHORIZED_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,401,"만료된 JWT 토큰입니다."),
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED,401,"JWT 토큰 검증 중 오류가 발생했습니다."),
    FORBIDDEN_TOKEN(HttpStatus.FORBIDDEN, 403, "관리자 권한이 없습니다."),
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, 404, "JWT 토큰이 필요합니다."),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 404, "재발급하려면 리프레쉬 토큰이 필요합니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, 400, "잘못된 토큰 형식 입니다."),
    NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 400, "리프레쉬 토큰이 아닙니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,401,"만료된 리프레쉬 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 400, "잘못된 리프레쉬 토큰입니다"),
    FAILED_TO_AUTHORIZE_USER(HttpStatus.INTERNAL_SERVER_ERROR, 500,"JWT 토큰 검증 중 오류가 발생했습니다."),

    // 유저 관련 예외
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 유저입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "유저를 찾을 수 없습니다."),
    USER_DELETED(HttpStatus.BAD_REQUEST,400,"탈퇴된 유저입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,400,"잘못된 비밀번호 입니다."),
    ALREADY_ATTEND(HttpStatus.FORBIDDEN,403,"이미 출석한 유저입니다."),
    ACCOUNT_BLOCKED(HttpStatus.BAD_REQUEST,400,"차단된 계정 입니다."),

    // 게시글 관련 예외
    NOT_FOUND_BOARD(HttpStatus.NOT_FOUND, 404, "게시글을 찾을 수 없습니다."),
    NOT_BOARD_WRITER(HttpStatus.UNAUTHORIZED, 401, "게시글 작성자가 아닙니다."),

    // 첨부파일 관련 예외
    NOT_UNIQUE_FILENAME(HttpStatus.BAD_REQUEST, 400, "중복된 이름의 첨부파일이 존재합니다."),
    ATTACHMENT_NAME_IS_EMPTY(HttpStatus.BAD_REQUEST, 400, "첨부파일 이름은 공백이 될 수 없습니다."),
    ATTACHMENT_NAME_IS_NULL(HttpStatus.BAD_REQUEST, 400, "첨부파일 이름이 NULL이 될 수 없습니다."),
    EMPTY_ATTACHMENT_LIST(HttpStatus.BAD_REQUEST, 400, "추가된 첨부파일이 존재하지 않습니다."),
    MAX_UPLOAD_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, 400, "단일 첨부파일은 최대 5MB까지 업로드 가능합니다."),
    MAX_UPLOAD_REQUEST_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, 400, "전체 첨부파일 용량은 10MB까지 업로드 가능합니다."),
    FAILED_TO_UPLOAD_ATTACHMENT(HttpStatus.INTERNAL_SERVER_ERROR, 500, "첨부 파일 업로드를 실패하였습니다."),
    FAILED_TO_DELETE_ATTACHMENT(HttpStatus.INTERNAL_SERVER_ERROR, 500, "첨부 파일 삭제를 실패하였습니다."),
    FILENAME_IS_TOO_LONG(HttpStatus.BAD_REQUEST, 400, "첨부파일명이 너무 깁니다. 25자 이내로 줄여서 업로드 해 주십시오."),

    // 댓글 관련 예외
    NOT_THE_AUTHOR(HttpStatus.NOT_ACCEPTABLE, 406, "게시물 작성자가 아닙니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 400, "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야 하며, 최소 8글자 이상이어야 합니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 400, "중복된 Email 입니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,404,"댓글을 찾지 못했습니다"),
    NOT_FOUND_COMMENT_LIST(HttpStatus.NOT_FOUND,404,"댓글 리스트를 찾지 못했습니다"),
    ALREADY_ADOPTED_COMMENT(HttpStatus.BAD_REQUEST, 400, "이미 채택된 댓글입니다"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,400,"유저를 찾지 못했습니다."),

    NOT_FOUND_FRAMEWORK(HttpStatus.NOT_FOUND, 404, "프레임워크를 찾지 못했습니다."),
    NOT_FOUND_USER_FRAMEWORK(HttpStatus.NOT_FOUND, 404, "유저의 프레임워크를 찾지 못했습니다."),
    ALREADY_ASSIGNED_USER_FRAMEWORK(HttpStatus.CONFLICT, 409, "유저의 프레임워크가 중복됩니다."),

    NOT_FOUND_LANGUAGE(HttpStatus.NOT_FOUND, 400, "언어를 찾지 못했습니다."),
    ALREADY_ASSIGNED_USER_LANGUAGE(HttpStatus.CONFLICT, 409, "유저의 언어가 중복됩니다."),
    NOT_FOUND_USER_LANGUAGE(HttpStatus.NOT_FOUND,400, "유저의 프레임워크를 찾지 못했습니다."),

    // json parsing
    JSON_CHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,500,"json 형식으로 바꾸는 것을 실패 했습니다."),
    JSON_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,500,"json 형식을 읽는 것을 실패 했습니다."),

    // 빌더 관련 예외
    ID_CANNOT_BE_SET(HttpStatus.BAD_REQUEST, 400, "ID 필드는 설정할 수 없습니다."),
    IS_DELETED_CANNOT_BE_SET(HttpStatus.BAD_REQUEST, 400, "isDeleted 필드는 설정할 수 없습니다."),
    CREATED_AT_CANNOT_BE_SET(HttpStatus.BAD_REQUEST, 400, "createdAt 필드는 설정할 수 없습니다."),

    // 채팅방 관련 예외
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, 404, "채팅방을 찾을 수 없습니다."),
    FAILED_TO_CREATE_CHAT_ROOM(HttpStatus.INTERNAL_SERVER_ERROR, 500, "채팅방 생성을 실패했습니다."),
    ALREADY_IN_CHATROOM(HttpStatus.CONFLICT, 409, "이미 채팅방에 접속해 있습니다."),
    ACCESS_DENIED_NOT_CORRECT_PASSWORD(HttpStatus.FORBIDDEN, 403, "잘못된 채팅방 비밀번호입니다."),
    ROOM_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, 409, "채팅방 정원이 초과되었습니다."),
    NOT_IN_CHATROOM(HttpStatus.CONFLICT, 409, "현재 채팅방에 접속해 있지 않습니다."),
    NOT_CHATROOM_HOST(HttpStatus.UNAUTHORIZED, 401, "채팅방 방장이 아닙니다."),
    NOT_FOUND_CHAT_USER(HttpStatus.NOT_FOUND, 404, "채팅에 접속되어 있지 않습니다."),
    NO_USER_IN_CHATROOM(HttpStatus.INTERNAL_SERVER_ERROR, 500, "채팅방에 유저가 없습니다."),
    UNAUTHORIZED_CHAT_USER(HttpStatus.UNAUTHORIZED, 401, "채팅방 접속 인증 유저 정보를 찾을 수 없습니다."),
    INVALID_CHAT_USER_ROLE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 채팅 유저 권한입니다."),
    CHATROOM_IS_EMPTY(HttpStatus.NOT_FOUND, 404, "채팅방에 접속 중인 유저가 존재하지 않습니다."),

    // 메세지 관련 예외
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 메세지 분류입니다."),
    FAILED_TO_SEND_MESSAGE(HttpStatus.INTERNAL_SERVER_ERROR, 500, "메세지 전송을 실패하였습니다."),
    INVALID_STOMP_DESTINATION_KEY(HttpStatus.BAD_REQUEST, 400, "STOMP 프로토콜 Destination 경로 키워드가 아닙니다."),
    INVALID_ROUTING_KEY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 메세지 브로커의 RoutingKey 입니다."),

    // DB 관련 예외
    SQL_EXCEPTION_OCCURRED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "데이터베이스 작업 처리 중 예외가 발생했습니다."),
    INVALID_REDIS_KEY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 Redis Key 입니다."),

    // Validation 예외
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 400, "입력값이 유효하지 않습니다."),

    // 낙관적 락 예외 처리
    OPTIMISTIC_LOCK_FAILED(HttpStatus.NO_CONTENT, 204, "다시 시도해 주세요."),
    EVENT_END(HttpStatus.GONE, 410, "이벤트가 종료되었습니다."),
    NO_ID_OF_KEY(HttpStatus.BAD_REQUEST, 400, "해당 키의 ID가 존재하지 않습니다."),

    // 알람 관련 예외 처리
    NOT_FOUND_ALARM(HttpStatus.NOT_FOUND, 404, "유저의 알림이 존재하지 않습니다."),
    NOT_FOUND_ALARM_UNREAD(HttpStatus.NOT_FOUND, 404, "읽지 않은 알림이 존재하지 않습니다.");

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
