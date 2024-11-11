package com.sparta.codechef.domain.chat.v3_redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Subscribe {
    SEND_PREFIX("/app"),
    INIT("/init/"),
    TOPIC_PREFIX("/topic"),
    CHAT_ROOM("/chat-room/");

    private final String key;


    public String getKey() {
        return this.key;
    }
}
