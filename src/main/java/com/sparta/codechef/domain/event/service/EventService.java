package com.sparta.codechef.domain.event.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Void eventStart(AuthUser authUser) {
//        if (authUser.getAuthorities().stream().anyMatch(
//                grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.ROLE_ADMIN.getUserRole())))
//            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);

        Integer eventCoin = 100;
        redisTemplate.opsForValue().set("event", eventCoin, Duration.ofHours(1));
        return null;
    }

    @Transactional
    public Void eventPoints(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        String lockKey = "event-lock";
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(2));

        if (lockAcquired != null && lockAcquired) {
            try {
                Long event = redisTemplate.opsForValue().decrement("event");
                if (event != null && event >= 0) {
                    checkAttendance(user, 1000);
                    user.updateLastAttendDate();
                } else {
                    redisTemplate.opsForValue().set("event", 0);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        return null;
    }

    @Transactional
    public void checkAttendance(User user, int points) {
        String redisKey = "user:" + user.getId() + ":points";
        redisTemplate.opsForValue().increment(redisKey, points);
        redisTemplate.expire(redisKey, Duration.ofMinutes(10));
        syncPointsToDatabase();
    }

    @Transactional
    public void syncPointsToDatabase() {
        redisTemplate.keys("user:*:points").forEach(redisKey -> {
            String userIdString = redisKey.replaceAll("user:(\\d+):points", "$1");
            long id = Long.parseLong(userIdString);
            int points = (int) redisTemplate.opsForValue().get(redisKey);
            userRepository.updatePoints(points, id);
            redisTemplate.delete(redisKey);
        });
    }
}
