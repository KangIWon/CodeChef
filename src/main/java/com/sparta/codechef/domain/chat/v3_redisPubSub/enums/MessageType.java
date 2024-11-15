package com.sparta.codechef.domain.chat.v3_redisPubSub.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum MessageType {
    CREATE("채팅방이 개설되었습니다."),
    IN("[ ${EMAIL} ] 님이 채팅방에 입장하셨습니다."),
    OUT("[ ${EMAIL} ] 님이 채팅방에서 퇴장하셨습니다."),
    NEW_HOST("[ ${EMAIL} ] 님이 방장이 되셨습니다."),
    SEND("");


    private final String info;

    public static MessageType of(String type) {
        return Arrays.stream(MessageType.values())
                .filter(messageType -> messageType.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_MESSAGE_TYPE));
    }

    public String toString(String email) {
        return this.info.replace("${EMAIL}", email);
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
