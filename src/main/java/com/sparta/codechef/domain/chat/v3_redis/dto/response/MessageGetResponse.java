package com.sparta.codechef.domain.chat.v3_redis.dto.response;

import com.sparta.codechef.domain.chat.v3_redis.dto.MessageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageGetResponse {
    private final String id;
    private final String type;
    private final String roomId;
    private final ChatUserGetResponse sender;
    private final String content;
    private final String createdAt;

    public MessageGetResponse(MessageDto messageDto) {
        this.id = messageDto.getId().toString();
        this.type = messageDto.getType();
        this.roomId = messageDto.getRoomId().toString();
        this.sender = new ChatUserGetResponse(messageDto.getSenderId().toString(), messageDto.getSenderEmail());
        this.content = messageDto.getContent();
        this.createdAt = messageDto.getCreatedAt();
    }
}
