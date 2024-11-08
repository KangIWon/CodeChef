package com.sparta.codechef.domain.alarm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void sendNotification(String message) {
        // "notifications" 채널에 메시지 발행
        redisTemplate.convertAndSend("notifications", message);
    }
}
