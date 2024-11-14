package com.sparta.codechef.domain.chat.v1.dto.request;


import lombok.Getter;

@Getter
public class ChatRoomRequest {

    private final String title;
    private final String password;
    private final int maxParticipants;

    public ChatRoomRequest(String title, String password, int maxParticipants) {
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants;
    }
}
