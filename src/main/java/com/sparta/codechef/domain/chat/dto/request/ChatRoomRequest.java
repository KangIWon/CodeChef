package com.sparta.codechef.domain.chat.dto.request;

import lombok.Getter;

@Getter
public class ChatRoomRequest {

    private final String title;
    private final String password;
    private final Integer maxParticipants;

    public ChatRoomRequest(String title, String password, Integer maxParticipants) {
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants;
    }
}
