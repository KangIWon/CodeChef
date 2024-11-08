package com.sparta.codechef.domain.chat.v3_redis.dto;

import com.sparta.codechef.domain.chat.v3_redis.entity.Message;
import com.sparta.codechef.domain.chat.v3_redis.enums.MessageType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("message")
public class MessageDto implements Serializable {
    @Id
    private Long id;
    @NotNull
    private MessageType type;
    @NotNull
    private Long roomId;
    private Long senderId;
    private String senderEmail;
    private String content;
    @NotNull
    private LocalDateTime createdAt;

    private MessageDto(Long id, MessageType type, Long roomId, Long senderId, String senderEmail, String content, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.createdAt = createdAt;
    }

    public MessageDto(Message message) {
        this.id = message.getId();
        this.type = message.getType();
        this.roomId = message.getRoomId();
        this.senderId = message.getUserId();
        this.senderEmail = message.getEmail();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }

    public String getType() {
        return this.type.name();
    }

    public String getCreatedAt() {
        return this.createdAt.toString();
    }

    public void setType(String type) {
        this.type = MessageType.of(type);
    }

    public void setCreatedAt(String createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = LocalDateTime.parse(createdAt, formatter);
    }
}
