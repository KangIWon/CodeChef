package com.sparta.codechef.domain.chat.v3_redisPubSub.dto.request;

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
