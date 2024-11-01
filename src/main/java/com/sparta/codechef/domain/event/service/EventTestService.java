package com.sparta.codechef.domain.event.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EventTestService {

    private final UserRepository userRepository;

    private final EventService eventService;

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

//    @Retryable(
//            retryFor = OptimisticLockingFailureException.class, // 재시도할 예외
//            maxAttempts = 3,                                 // 최대 재시도 횟수
//            backoff = @Backoff(delay = 200)                  // 재시도 간격 (밀리초)
//    )
    /*********** 1. 낙관적 락을 사용한 포인트 증가 **********************/
    public int eventPointsOptimisticLock(Long id) {
        boolean success = false;
        int failureCount = 0; // 실패 횟수 기록

        while (!success) {
            try {
                // 트랜잭션 내에서 최신 상태로 조회하여 업데이트
                update(id);
                success = true;
            } catch (Exception e) {
                failureCount++;
            }
        }
        return failureCount;
    }

    @Transactional
    public void update(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );
        user.eventAddPoint(); // 포인트 증가 로직
        userRepository.save(user); // 변경 사항 저장
    }








    /*********** 2. 비관적 락을 사용한 포인트 증가 **********************/
    @Transactional
    public Void eventPointsPessimisticLock(Long id) {
        User user = userRepository.findByIdWithPessimisticLock(id).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        user.eventAddPoint(); // User 엔티티의 포인트 증가 메서드
        user.updateLastAttendDate();
        userRepository.save(user);
        return null;
    }


    /** 3. 데이터베이스에 즉시 포인트를 저장하는 방식 **/
    @Transactional
    public Void eventPointsDirectDB(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        user.eventAddPoint(); // User 엔티티의 포인트 증가 메서드
        user.updateLastAttendDate();
        userRepository.save(user);
        return null;
    }

    /** 4. 루아스크립트를 이용한 포인트 저장하는 방식 */
    @Transactional
    public Void luaScriptEventPoints(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );
        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        String eventKey = "event";

        // Lua script for atomic decrement
        String luaScript = "local current = tonumber(redis.call('get', KEYS[1]))\n" +
                "if not current then\n" +
                "    return false\n" +
                "end\n" +
                "if current > 0 then\n" +
                "    redis.call('decr', KEYS[1])\n" +
                "    return current - 1\n" +
                "else\n" +
                "    return false\n" +
                "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);

        // Execute the script
        Long result = redisTemplate.execute(script, Collections.singletonList(eventKey));

        if (result == null || result < 0) {
            throw new ApiException(ErrorStatus.EVENT_END);
        }

        eventService.checkAttendance(user, 1000); // Example points value
        user.updateLastAttendDate();

        return null;
    }

    /** 4. 루아스크립트를 이용한 포인트 저장하는 방식 */
    @Transactional
    public Void luaScriptEventPoints2(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );
        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }

        String eventKey = "event";

        // Lua script for atomic decrement
        String luaScript = "local current = tonumber(redis.call('get', KEYS[1]))\n" +
                "if not current then\n" +
                "    return false\n" +
                "end\n" +
                "if current > 0 then\n" +
                "    redis.call('decr', KEYS[1])\n" +
                "    return current - 1\n" +
                "else\n" +
                "    return false\n" +
                "end";


        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);

        // Execute the script
        Long result = redisTemplate.execute(script, Collections.singletonList(eventKey));

        if (result == null || result < 0) {
            throw new ApiException(ErrorStatus.EVENT_END);
        }

        eventService.checkAttendance(user, 1000); // Example points value
        user.updateLastAttendDate();

        return null;
    }


    /** 5. 분산락을 이용한 포인트 저장 */
    @Transactional
    public Void eventPoints(Long id) {
        // 업데이트를 위해 유저를 조회
        User user = userRepository.findById(id).orElseThrow(
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
                        eventService.checkAttendance(user, 1000);
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
}
