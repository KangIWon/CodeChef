package com.sparta.codechef.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }
    @Nested
    @DisplayName("랭킹 스케줄러")
    class RankingScheduler {
        @Test
        void saveRankingTop3LastMonth() {
            // Given: 빌더 패턴을 사용해 3명의 유저 생성
            User user1 = User.builder()
                    .id(1L)
                    .email("user1@example.com")
                    .password("password1")
                    .point(100)
                    .organization(Organization.EMPLOYED)
                    .build();

            User user2 = User.builder()
                    .id(2L)
                    .email("user2@example.com")
                    .password("password2")
                    .point(200)
                    .organization(Organization.UNEMPLOYED)
                    .build();

            User user3 = User.builder()
                    .id(3L)
                    .email("user3@example.com")
                    .password("password3")
                    .point(300)
                    .organization(Organization.EMPLOYED)
                    .build();

            List<User> allUsers = Arrays.asList(user1, user2, user3);

            when(userRepository.findAll()).thenReturn(allUsers);

            doAnswer(invocation -> {
                allUsers.forEach(user -> user.updatePoint(0));
                return null;
            }).when(userRepository).resetUserPoint();

            rankingService.saveRankingTop3LastMonth();

            verify(userRepository, times(1)).resetUserPoint();
            for (User user : allUsers) {
                assertEquals(0, user.getPoint(), "유저의 포인트는 0으로 리셋되어야 합니다.");
            }
            for (User user : allUsers) {
                String redisKey = user.checkRedisKey(user);
                verify(zSetOperations, times(1)).add(redisKey, user.getId(), 0.0);
            }
        }
    }
    /**
     * 테스트용 유저를 생성하는 유틸 메서드
     * @param email 유저 이메일
     * @param isAttended 초기 출석 상태
     * @return 생성된 User 객체
     */
    private User createTestUser(String email, int point, LocalDate lastAttendDate, boolean isAttended) {
        User user = User.builder()
                .email(email)
                .password("securePassword")
                .personalHistory("Java Developer")
                .userRole(UserRole.ROLE_USER)
                .organization(Organization.EMPLOYED)
                .point(point)
                .isAttended(isAttended)
                .build();

        // lastAttendDate 필드 값을 설정
        ReflectionTestUtils.setField(user, "lastAttendDate", lastAttendDate);
        return user;
    }
}