package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.domain.chat.v2.dto.response.ChatUserResponse;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@RedisHash("message")
public class WSMessage implements Serializable {
    @Id
    private Long id;
    private Long roomId;
    private ChatUserResponse sender;
    private String content;
    private LocalDateTime createdAt;

    public WSMessage(Long id, Long roomId, ChatUserResponse sender, String content) {
        this.id = id;
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public static WSMessage getMessage(Long roomId, String email, MessageType messageType) {
        return new WSMessage(null, roomId, null, messageType.toString(email));
    }
}
