package com.sparta.codechef.domain.chat.v1.dto.response;

import lombok.Getter;


@Getter
public class ChatRoomGetResponse {
    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final int curParticipants;
    private final int maxParticipants;

    public ChatRoomGetResponse(Long id, String title, String password, int curParticipants, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.isSecret = password != null;
        this.curParticipants = curParticipants;
        this.maxParticipants = maxParticipants;
    }
}
