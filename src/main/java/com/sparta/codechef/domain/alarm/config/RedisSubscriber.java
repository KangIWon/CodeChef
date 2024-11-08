package com.sparta.codechef.domain.alarm.config;

import com.sparta.codechef.domain.alarm.entity.Notification;
import com.sparta.codechef.domain.alarm.repository.NotificationRepository;
import com.sparta.codechef.domain.alarm.service.SlackService;
import com.sparta.codechef.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.MessageListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final SlackService slackService;
    private final UserRepository userRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        System.out.println("Received notification: " + notificationMessage);

        if (notificationMessage.contains("댓글")) {
            // 댓글 알림 처리 로직: 특정 사용자에게 전송
            Long userId = extractUserIdFromMessage(notificationMessage);
            if (userId != null) {
                Notification dbNotification = new Notification(userId, notificationMessage, false);
                notificationRepository.save(dbNotification);
                slackService.sendSlackMessage(notificationMessage);
                log.info("댓글");
                // WebSocket으로 특정 사용자에게만 전송
                messagingTemplate.convertAndSend("/queue/notifications/" + userId, notificationMessage);
            }
        } else if (notificationMessage.contains("이벤트")) {
            // 이벤트 알림 처리 로직: 모든 사용자에게 전송
            userRepository.findAll().forEach(user -> {
                Notification dbNotification = new Notification(user.getId(), notificationMessage, false);
                notificationRepository.save(dbNotification);
            });
            slackService.sendSlackMessage(notificationMessage);
            log.info("이벤트");
            // WebSocket으로 모든 사용자에게 브로드캐스트 전송
            messagingTemplate.convertAndSend("/topic/notifications", notificationMessage);
        }
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
        return null; // userId를 찾을 수 없으면 null 반환
    }
}
