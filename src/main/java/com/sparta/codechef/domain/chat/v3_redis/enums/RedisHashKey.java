package com.sparta.codechef.domain.chat.v3_redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisHashKey {

    // 채팅 유저
    ID("id"),
    ROLE("role"),
    EMAIL("email"),

    // 채팅방
    ROOM_ID("roomId"),
    CUR_PARTICIPANTS("curParticipants"),
    HOST_ID("hostId"),

    // 메세지
    SENDER("sender"),
    CONTENT("content"),
    CREATED_AT("createdAt")
    ;

    private final String hashKey;

    public String toString() {
        return this.hashKey;
    }
}
