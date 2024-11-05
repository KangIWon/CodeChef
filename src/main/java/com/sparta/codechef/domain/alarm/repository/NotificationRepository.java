package com.sparta.codechef.domain.alarm.repository;

import com.sparta.codechef.domain.alarm.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 읽지 않은 알림만 가져오기
    Optional<List<Notification>> findByUserIdAndIsReadFalse(Long userId);
    // 모든 알림 가져오기
    Optional<List<Notification>> findByUserId(Long userId);
}
