package com.sparta.codechef.domain.event.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
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
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public Void eventStart(AuthUser authUser) {
        if (!authUser.getUserRole().equals(UserRole.ROLE_ADMIN))
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);

        RAtomicLong eventCounter = redissonClient.getAtomicLong("event");
        eventCounter.set(100);
        eventCounter.expire(1, TimeUnit.HOURS);

        // Redis로 알림 메시지 발행
        String channel = "eventNotifications";
        String message = "이벤트가 시작되었습니다.";
        redisTemplate.convertAndSend(channel, message);
        return null;
    }

    @Transactional
    public Void eventPoints2(AuthUser authUser) {
        // 업데이트를 위해 유저를 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        // 해당 유저가 출석체크 했는지 확인
        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        // 분산락에 필요한 lock 부여
        RLock lock = redissonClient.getFairLock("event-lock");

        try {
            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    // Redis에서 event 값을 원자적으로 감소시키기
                    RAtomicLong eventCounter = redissonClient.getAtomicLong("event");
                    long event = eventCounter.decrementAndGet(); // 하나씩 감소

                    if (event >= 0) {
                        checkAttendance(user, 1000);
//                        user.isAttended();
                        user.updateLastAttendDate();
                    } else {
                        // 이벤트 종료 후, 남은 포인트가 없을 때
                        eventCounter.set(0); // 값이 음수로 내려가지 않도록 0으로 설정
                        throw new ApiException(ErrorStatus.EVENT_END);
                    }
                    return null;
                } finally {
                    lock.unlock(); // 작업이 완료되면 락 해제
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Transactional
    public void checkAttendance(User user, int points) {

        String redisKey = "user:" + user.getId() + ":points";
        redissonClient.getAtomicLong(redisKey).addAndGet(points); // Redis에 누적 점수 업데이트

        // TTL 설정을 위해 RBucket 사용
        RBucket<Object> bucket = redissonClient.getBucket(redisKey);
        bucket.expire(10, TimeUnit.MINUTES); // TTL을 10분으로 설정
        // 이벤트에서 받은 포인트 저장
        syncPointsToDatabase();
    }

    @Transactional
    public void syncPointsToDatabase() {
        // Redis에서 모든 사용자 점수 조회 및 DB 업데이트 로직 구현
        RKeys keys = redissonClient.getKeys(); // RKeys 객체를 얻음
        Iterable<String> matchingKeys = keys.getKeysByPattern("user:*:points"); // 패턴에 맞는 모든 키 조회

        for (String redisKey : matchingKeys) {
            String userIdString = redisKey.replaceAll("user:(\\d+):points", "$1");
            long id = Long.parseLong(userIdString);
            int points = (int) redissonClient.getAtomicLong(redisKey).getAndSet(0); // Redis 값 초기화 후 가져오기
            userRepository.updatePoints(points, id); // DB에 점수 업데이트
            redissonClient.getBucket(redisKey).delete();
        }

    }
}
