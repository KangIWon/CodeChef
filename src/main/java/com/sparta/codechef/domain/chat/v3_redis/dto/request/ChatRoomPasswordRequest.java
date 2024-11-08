package com.sparta.codechef.domain.chat.v3_redis.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ChatRoomPasswordRequest {
    private final String password;

    @JsonCreator
    public ChatRoomPasswordRequest(@JsonProperty("password") String password) {
        this.password = password;
    }
}
