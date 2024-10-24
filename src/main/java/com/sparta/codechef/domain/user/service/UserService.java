package com.sparta.codechef.domain.user.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.dto.response.UserPoint;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 자동으로 IsAttend false로 바꾸는 것
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetAllIsAttend() {
        userRepository.resetIsAttend();
    }

    // 자동으로 포인트 감소하는 것
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void decreasePointsAutomatically() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (shouldDecreasePoints(user)) {
                decreasePoints(user);
            }
        }
    }

    // 마지막 출석일로부터 7일 지났는지 확인하는 메서드
    private boolean shouldDecreasePoints(User user) {
        LocalDate lastAttendDate = user.getLastAttendDate();
        LocalDate currentDate = LocalDate.now();
        return lastAttendDate != null && ChronoUnit.DAYS.between(lastAttendDate, currentDate) >= 7;
    }

    // 포인트 10% 차감 및 마지막 차감일 업데이트 메서드
    private void decreasePoints(User user) {
        Integer currentPoint = user.getPoint();
        Integer decreasedPoint = currentPoint - (currentPoint / 10);

        user.updatePoint(decreasedPoint);
        user.updateLastAttendDate();
        userRepository.save(user);
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
        return null;
    }

    public UserPoint getUserPoint(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        Integer point = user.getPoint();
        return new UserPoint(point);
    }
}
