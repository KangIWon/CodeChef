package com.sparta.codechef.domain.chatRoom.dto.request;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ChatRoomRequest {

    private final String title;
    private final String password;
    private final int maxParticipants;

    public ChatRoomRequest(@Nullable String title, @Nullable String password, int maxParticipants) {
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants;
    }
}
