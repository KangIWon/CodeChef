package com.sparta.codechef.domain.alarm.config;

import com.sparta.codechef.domain.alarm.entity.Notification;
import com.sparta.codechef.domain.alarm.repository.NotificationRepository;
import com.sparta.codechef.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 댓글 알림 처리 - 특정 사용자에게 전송
    @RabbitListener(queues = "comment.notifications.queue")
    public void handleCommentNotification(String message) {
        Long userId = extractUserIdFromMessage(message);
        if (userId != null) {
            Notification dbNotification = new Notification(userId, message, false);
            notificationRepository.save(dbNotification);

            // WebSocket을 통해 특정 사용자에게 전송
            messagingTemplate.convertAndSend("/queue/notifications/" + userId, message);
        }
    }

    // 이벤트 알림 처리 - 모든 사용자에게 브로드캐스트 전송
    @RabbitListener(queues = "event.notifications.queue")
    public void handleEventNotification(String message) {
        userRepository.findAll().forEach(user -> {
            Notification dbNotification = new Notification(user.getId(), message, false);
            notificationRepository.save(dbNotification);
        });

        // WebSocket을 통해 모든 사용자에게 브로드캐스트 전송
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    private Long extractUserIdFromMessage(String message) {
        try {
            Pattern pattern = Pattern.compile("작성자 ID: (\\d+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("메시지에서 userId를 추출할 수 없습니다: " + e.getMessage());
        }
        return null;
    }
}
