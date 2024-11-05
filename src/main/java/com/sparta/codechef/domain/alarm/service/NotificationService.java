package com.sparta.codechef.domain.alarm.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.alarm.dto.response.NotificationResponse;
import com.sparta.codechef.domain.alarm.entity.Notification;
import com.sparta.codechef.domain.alarm.repository.NotificationRepository;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 모든 알림 가져오기
    public List<NotificationResponse> getAllNotificationsByUserId(AuthUser user) {
        return notificationRepository.findByUserId(user.getUserId())
                .filter(notifications -> !notifications.isEmpty()) // 알림이 비어 있지 않을 때만 반환
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_ALARM))
                .stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getUserId(),
                        notification.getMessage(),
                        notification.isRead()
                ))
                .collect(Collectors.toList());
    }

    // 읽지 않은 알림만 가져오기
    public List<NotificationResponse> getUnreadNotificationsByUserId(AuthUser user) {
        return notificationRepository.findByUserIdAndIsReadFalse(user.getUserId())
                .filter(notifications -> !notifications.isEmpty()) // 알림이 비어 있지 않을 때만 반환
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_ALARM_UNREAD))
                .stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getUserId(),
                        notification.getMessage(),
                        notification.isRead()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(AuthUser user, Long notificationId) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new ApiException(ErrorStatus.NOT_FOUND_USER);
        }
        // 알림이 사용자 본인에게 속해 있는지 확인
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_ALARM));

        notification.setRead(); // 읽음 상태로 변경
        notificationRepository.save(notification); // 변경된 상태 저장
    }
}
