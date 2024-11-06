package com.sparta.codechef.domain.event.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    private final RedissonClient redissonClient;


    public Void eventStart(AuthUser authUser) {


        if (!authUser.getUserRole().equals(UserRole.ROLE_ADMIN))
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);

        Integer eventCoin = 100;
        redisTemplate.opsForValue().set("event", eventCoin, Duration.ofHours(1));

        RAtomicLong eventCounter = redissonClient.getAtomicLong("event");
        eventCounter.set(100); // 초기값 설정
        eventCounter.expire(1, TimeUnit.HOURS); // 만료 시간 설정
        return null;
    }


    @Transactional
    public Void eventPoints(AuthUser authUser) {
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
                        user.isAttended();
                        user.updateLastAttendDate();
                    } else {
                        // 이벤트 종료 후, 남은 포인트가 없을 때
                        eventCounter.delete();
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
        redisTemplate.opsForValue().increment(redisKey, points);
        redisTemplate.expire(redisKey, Duration.ofMinutes(10));

        // 이벤트에서 받은 포인트를 DB에 저장
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
