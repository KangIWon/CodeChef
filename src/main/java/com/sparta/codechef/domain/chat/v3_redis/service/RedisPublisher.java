package com.sparta.codechef.domain.chat.v3_redis.service;

import com.sparta.codechef.domain.chat.v3_redis.dto.response.MessageGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.sparta.codechef.domain.chat.v3_redis.enums.RedisHashKey.*;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, MessageGetResponse message) {
        redisTemplate.convertAndSend(topic.getTopic(), this.convertMessageToMap(message));
    }

    public void publish(ChannelTopic topic, List<MessageGetResponse> messageList) {
        messageList.forEach(messageGetResponse ->
                redisTemplate.convertAndSend(topic.getTopic(), this.convertMessageToMap(messageGetResponse))
        );
    }

    public void publish(ChannelTopic topic, String data) {
        redisTemplate.convertAndSend(topic.getTopic(), data);
    }

    private Map<String, String> convertMessageToMap(MessageGetResponse message) {
        return Map.of(
                ID.getHashKey(), message.getId(),
                ROOM_ID.getHashKey(), message.getRoomId(),
                SENDER.getHashKey(), message.getSender() == null ? "CodeChef" : message.getSender().getEmail(),
                CONTENT.getHashKey(), message.getContent(),
                CREATED_AT.getHashKey(), message.getCreatedAt()
        );
    }
}
