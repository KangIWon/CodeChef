package com.sparta.codechef.domain.chat.v3_redisPubSub.entity;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.MessageType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ChatMessage {
    private final Long id;
    private final MessageType type;
    private final Long roomId;
    private final Long userId;
    private final String email;
    private final String content;
    private final LocalDateTime createdAt;

    private ChatMessage(Long id, MessageType type, Long roomId, Long userId, String email, String content) {
        this.id = id;
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.email = email;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    private ChatMessage(Long id, MessageType type, Long roomId, Long userId, String email, String content, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.email = email;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatMessage fromMessageDto(MessageDto messageDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new ChatMessage(
                messageDto.getId(),
                MessageType.of(messageDto.getType()),
                messageDto.getRoomId(),
                messageDto.getSenderId(),
                messageDto.getSenderEmail(),
                messageDto.getContent(),
                LocalDateTime.parse(messageDto.getCreatedAt(), formatter)
        );
    }

    public static ChatMessage getMessage(Long id, MessageType type, Long roomId, String email) {
        if (email == null) {
            return new ChatMessage(id, type, roomId, 0L, "CodeChef", type.getInfo());
        }

        return new ChatMessage(id, type, roomId, 0L, "CodeChef", type.toString(email));
    }

    public static ChatMessage getMessage(Long id, Long roomId, ChatUser chatUser, String content) {
        return new ChatMessage(id, MessageType.SEND, roomId, chatUser.getId(), chatUser.getEmail(), content);
    }
}
