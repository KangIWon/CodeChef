package com.sparta.codechef.domain.chat.v3_redisPubSub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String destination = new String(message.getChannel());
        String payload = new String(message.getBody());
        log.info("destination: {}, payload: {}", destination, payload);

        this.messagingTemplate.convertAndSend(destination, payload);
    }
}
