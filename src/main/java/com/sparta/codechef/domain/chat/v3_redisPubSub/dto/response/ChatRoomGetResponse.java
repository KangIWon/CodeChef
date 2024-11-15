package com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response;

import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomGetResponse {
    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final int curParticipants;
    private final int maxParticipants;

    public ChatRoomGetResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.isSecret = chatRoom.getPassword() != null;
        this.curParticipants = chatRoom.getCurParticipants();
        this.maxParticipants = chatRoom.getMaxParticipants();
    }
}
