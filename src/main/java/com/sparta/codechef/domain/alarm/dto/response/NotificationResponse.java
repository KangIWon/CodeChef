package com.sparta.codechef.domain.alarm.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId; // 알림 대상 사용자 ID
    private String message;
    private boolean isRead; // 읽음 여부

    public NotificationResponse(Long id, Long userId, String message, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
    }
}
