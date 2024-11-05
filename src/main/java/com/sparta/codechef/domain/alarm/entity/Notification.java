package com.sparta.codechef.domain.alarm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 알림 대상 사용자 ID
    private String message;
    private boolean isRead = false; // 읽음 여부

    public Notification(Long userId, String message, boolean isRead) {
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
    }

    public void setRead() {
        this.isRead = true;
    }
}
