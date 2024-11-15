package com.sparta.codechef.domain.chat.v3_redisPubSub.entity;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatRoomDto;
import lombok.Getter;

@Getter
public class ChatRoom {
    private final Long id;
    private final String title;
    private final String password;
    private final int maxParticipants;
    private final int curParticipants;
    private final Long hostId;  // 방장

    private ChatRoom(Long id, String title, String password, int maxParticipants, int curParticipants, Long hostId) {
        this.id = id;
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants;
        this.curParticipants = curParticipants;
        this.hostId = hostId;
    }

    public ChatRoom updateRoomInfo(String title, String password, int maxParticipants) {
        return new ChatRoom(
                this.id,
                title != null ? title : this.title,
                password,
                maxParticipants != 0 ? maxParticipants : this.maxParticipants,
                this.curParticipants,
                this.hostId
        );
    }

    public static ChatRoom of(ChatRoomDto chatRoom) {
        return new ChatRoom(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getPassword(),
                chatRoom.getMaxParticipants(),
                chatRoom.getCurParticipants(),
                chatRoom.getHostId()
        );
    }

    public ChatRoom updateHost(Long nextHostId) {
        return new ChatRoom(this.id, this.title, this.password, this.getMaxParticipants(),this.curParticipants, nextHostId);
    }
}
