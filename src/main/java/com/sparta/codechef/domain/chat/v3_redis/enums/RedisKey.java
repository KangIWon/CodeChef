package com.sparta.codechef.domain.chat.v3_redis.enums;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKey {
    // 채팅방
    ID_CHAT_ROOM("id:chatRoom"),
    SUBSCRIBE_CHAT_ROOM("subChatRoom"),
    CHAT_ROOM("chatRoom:"),

    // 채팅 유저
    ID_CHAT_USER("id:chatUser"),
    CHAT_USER("chatUser:"),

    // 메세지
    ID_MESSAGE("id:message"),
    CHAT_ROOM_MESSAGE("chatRoomMessages");

    private final String key;


    public String toString() {
        return this.key;
    }

    public String getKey(Long id) {
        if (this.equals(CHAT_ROOM) || this.equals(CHAT_USER)) {
            return new StringBuffer()
                    .append(this.key)
                    .append(id)
                    .toString();
        }

        throw new ApiException(ErrorStatus.INVALID_REDIS_KEY);
    }
}
