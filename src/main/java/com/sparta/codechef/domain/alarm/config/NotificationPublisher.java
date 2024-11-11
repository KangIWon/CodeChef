package com.sparta.codechef.domain.alarm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    // 댓글 알림 발행
    public void sendCommentNotification(String message) {
        rabbitTemplate.convertAndSend("comment.notifications.exchange", "comment.notifications.key", message);
    }

    // 이벤트 알림 발행
    public void sendEventNotification(String message) {
        rabbitTemplate.convertAndSend("event.notifications.exchange", "event.notifications.key", message);
    }
}
