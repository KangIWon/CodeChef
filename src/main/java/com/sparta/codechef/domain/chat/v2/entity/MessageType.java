package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    CREATE("채팅방이 개설되었습니다.\n[ ${EMAIL} ] 님이 방장입니다."),
    IN("[ ${EMAIL} ] 님이 채팅방에 입장하셨습니다."),
    OUT("[ ${EMAIL} ] 님이 채팅방에서 퇴장하셨습니다."),
    NEW_HOST("[ ${EMAIL} ] 님이 새로운 방장이 되셨습니다.");


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
}
