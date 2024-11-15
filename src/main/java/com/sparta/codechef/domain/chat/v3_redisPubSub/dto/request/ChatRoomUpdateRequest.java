package com.sparta.codechef.domain.chat.v3_redisPubSub.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomUpdateRequest {
    private final String title;
    private final String password;
    private final int maxParticipants;
}
