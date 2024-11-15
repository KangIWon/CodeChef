package com.sparta.codechef.domain.chat.v3_redisPubSub.service;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.MessageGetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisHashKey.*;

@Slf4j
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

    /**
     * MessageGetResponse 객체를 Map으로 변환
     * @param message : 발행할 메세지 객체
     * @return
     */
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
