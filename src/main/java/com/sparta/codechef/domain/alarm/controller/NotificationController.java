package com.sparta.codechef.domain.alarm.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.alarm.dto.response.NotificationResponse;
import com.sparta.codechef.domain.alarm.service.NotificationService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 모든 알림 가져오기
    @GetMapping("/all")
    public ApiResponse<List<NotificationResponse>> getAllNotifications(@AuthenticationPrincipal AuthUser user) {
        List<NotificationResponse> notifications = notificationService.getAllNotificationsByUserId(user);
        return ApiResponse.ok("모든 알림을 성공적으로 가져왔습니다.", notifications);
    }

    // 읽지 않은 알림만 가져오기
    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(@AuthenticationPrincipal AuthUser user) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotificationsByUserId(user);
        return ApiResponse.ok("읽지 않은 알림을 성공적으로 가져왔습니다.", notifications);
    }

    // 알림 읽음 처리
    @PutMapping("/read/{notificationId}")
    public ApiResponse<Void> markNotificationAsRead(@AuthenticationPrincipal AuthUser user, @PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(user, notificationId);
        return ApiResponse.ok("알림이 읽음 상태로 변경되었습니다.", null);
    }
}
