package com.sparta.codechef.domain.chat.v4_rabbitMQ.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQChatConsumer implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message) {
        String destination = message.getMessageProperties().getHeader("destination");
        String payload = new String(message.getBody());
        messagingTemplate.convertAndSend(destination, payload);
    }
}
