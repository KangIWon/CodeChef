package com.sparta.codechef.domain.alarm.service;

import com.sparta.codechef.domain.alarm.entity.Notification;
import com.sparta.codechef.domain.alarm.repository.NotificationRepository;
import com.sparta.codechef.domain.user.repository.UserRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class EventNotificationSubscriber implements MessageListener {

    private final NotificationRepository notificationRepository;
    private final SlackService slackService;
    private final UserRepository userRepository; // 모든 유저를 조회하기 위해 추가

    public EventNotificationSubscriber(NotificationRepository notificationRepository,
                                       SlackService slackService,
                                       UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.slackService = slackService;
        this.userRepository = userRepository;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        System.out.println("새 이벤트 알림 수신: " + notificationMessage);

        // 모든 사용자에게 알림 저장 및 Slack 알림 전송
        userRepository.findAll().forEach(user -> {
            Notification dbNotification = new Notification(user.getId(), notificationMessage, false);
            notificationRepository.save(dbNotification);
            System.out.println("알림 저장 완료 - 사용자 ID: " + user.getId()); // 각 사용자에게 알림 저장 로그
        });

        // Slack으로 알림 전송
        slackService.sendSlackMessage(notificationMessage);
    }
}

