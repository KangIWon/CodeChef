package com.sparta.codechef.domain.chat.dto.response;

import lombok.Getter;


@Getter
public class ChatRoomGetResponse {
    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final long curParticipants;
    private final int maxParticipants;

    public ChatRoomGetResponse(Long id, String title, boolean isSecret, long curParticipants, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.isSecret = isSecret;
        this.curParticipants = curParticipants;
        this.maxParticipants = maxParticipants;
    }
}
