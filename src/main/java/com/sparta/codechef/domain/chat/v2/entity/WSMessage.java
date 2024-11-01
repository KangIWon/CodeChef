package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.domain.chat.v2.dto.response.ChatUserResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


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

    public WSMessage(Long id, Long roomId, ChatUserResponse sender, String content, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.createdAt = Objects.requireNonNullElseGet(createdAt, LocalDateTime::now);
    }
}
