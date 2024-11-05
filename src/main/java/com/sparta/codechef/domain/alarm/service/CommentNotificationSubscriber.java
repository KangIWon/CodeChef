package com.sparta.codechef.domain.alarm.service;

import com.sparta.codechef.domain.alarm.entity.Notification;
import com.sparta.codechef.domain.alarm.repository.NotificationRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentNotificationSubscriber implements MessageListener {

    private final NotificationRepository notificationRepository;
    private final SlackService slackService;

    public CommentNotificationSubscriber(NotificationRepository notificationRepository, SlackService slackService) {
        this.notificationRepository = notificationRepository;
        this.slackService = slackService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        System.out.println("새 댓글 알림 수신: " + notificationMessage);

        // 알림 DB 저장 (userId를 전달)
        Long userId = extractUserIdFromMessage(notificationMessage);  // 메시지에서 userId 추출 메서드 예시
        Notification dbNotification = new Notification(userId, notificationMessage, false);
        notificationRepository.save(dbNotification);

        // Slack으로 알림 전송
        slackService.sendSlackMessage(notificationMessage);
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
