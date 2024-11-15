package com.sparta.codechef.domain.user.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.dto.response.UserPoint;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetAllIsAttend() {
        userRepository.resetIsAttend();
    }

    // 일주일 미출첵시, 자동으로 포인트 감소
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void decreasePointsAutomatically() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.decreaseAutomatically(sevenDaysAgo, today);
        for (User user : users) {
            String redisKey = user.checkRedisKey(user);
            redisTemplate.opsForZSet().add(redisKey, user.getId(), user.getPoint());
        }
    }

    @Transactional
    public Void creditPoints(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        if (user.getIsAttended()) {
            throw new ApiException(ErrorStatus.ALREADY_ATTEND);
        }
        user.changeIsAttend();
        user.updateLastAttendDate();
        user.addPoint();
        userRepository.save(user);

        String redisKey = user.checkRedisKey(user);
        redisTemplate.opsForZSet().add(redisKey, user.getId(), user.getPoint());
        return null;
    }

    public UserPoint getUserPoint(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        Integer point = user.getPoint();
        return new UserPoint(point);
    }

}

