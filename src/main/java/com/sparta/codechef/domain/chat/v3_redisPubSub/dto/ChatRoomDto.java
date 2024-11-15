package com.sparta.codechef.domain.chat.v3_redisPubSub.dto;

import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatRoom;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("chatRoom")
public class ChatRoomDto implements Serializable {
    @Id
    private Long id;
    @NotNull
    private String title;
    private String password;
    @NotNull
    private int maxParticipants;
    @NotNull
    private int curParticipants;
    @NotNull
    private Long hostId;  // 방장

    public static ChatRoomDto fromChatRoom(ChatRoom chatRoom) {
        return new ChatRoomDto(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getPassword(),
                chatRoom.getMaxParticipants(),
                chatRoom.getCurParticipants(),
                chatRoom.getHostId()
        );
    }
}
