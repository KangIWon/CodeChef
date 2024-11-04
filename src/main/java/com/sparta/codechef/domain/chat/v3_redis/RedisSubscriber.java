package com.sparta.codechef.domain.chat.v3_redis;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    public void consume(String destination, String message) {
        messagingTemplate.convertAndSend(destination, message);
    }
}
