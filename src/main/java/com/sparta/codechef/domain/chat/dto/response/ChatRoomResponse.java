package com.sparta.codechef.domain.chat.dto.response;

import com.sparta.codechef.domain.chat.entity.ChatRoom;
import lombok.Getter;

@Getter
public class ChatRoomResponse {
    private Long id;
    private String title;
    private boolean isSecret;
    private int maxParticipants;

    public ChatRoomResponse(ChatRoom chatRoom)
    {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.isSecret = chatRoom.getPassword() != null;
        this.maxParticipants = chatRoom.getMaxParticipants();
    }
}
