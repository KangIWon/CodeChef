package com.sparta.codechef.domain.chat.v2.dto.request;

import lombok.Getter;

@Getter
public class ChatUserRequest {
    private final Long id;
    private final String email;

    public ChatUserRequest(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
