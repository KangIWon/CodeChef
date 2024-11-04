package com.sparta.codechef.domain.chat.v1.dto.response;

import com.sparta.codechef.domain.chat.v1.entity.ChatRoom;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import lombok.Getter;

@Getter
public class ChatRoomResponse {
    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final int maxParticipants;
    private final String wsUrl;

    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.isSecret = chatRoom.getPassword() != null;
        this.maxParticipants = chatRoom.getMaxParticipants();
        this.wsUrl = "/topic/chat-room/" + chatRoom.getId();
    }

    public ChatRoomResponse(WSChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.isSecret = chatRoom.getPassword() != null;
        this.maxParticipants = chatRoom.getMaxParticipants();
        this.wsUrl = "/topic/chat-room/" + chatRoom.getId();
    }
}
