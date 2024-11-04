package com.sparta.codechef.domain.chat.redis_pub_sub;

import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> template;

    public void publish(ChannelTopic topic, WSMessage message) {
        template.convertAndSend(topic.getTopic(), message);
    }

    public void publish(ChannelTopic topic, String data) {
        template.convertAndSend(topic.getTopic(), data);
    }
}
