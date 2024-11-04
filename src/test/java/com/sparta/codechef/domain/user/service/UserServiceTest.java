package com.sparta.codechef.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisTemplate<String,Object> redisTemplate;

    @Nested
    @DisplayName("유저 스케줄러")
    class UserScheduler {

        @Test
        void 유저_출석여부_하루마다_false로_변경_성공() {
            // Given: 테스트 데이터를 생성합니다.
            User user1 = createTestUser("example1@example.com",100,LocalDate.now().minusDays(1),true);
            User user2 = createTestUser("example2@example.com",100,LocalDate.now().minusDays(1),true);
            User user3 = createTestUser("example3@example.com",100,LocalDate.now().minusDays(1),true);

            List<User> userList = Arrays.asList(user1, user2, user3);

            // resetIsAttend가 호출될 때 isAttended 필드를 false로 설정하는 모킹
            doAnswer(invocation -> {
                userList.forEach(user -> ReflectionTestUtils.setField(user, "isAttended", false));
                return null;
            }).when(userRepository).resetIsAttend();

            // When: resetAllIsAttend 메서드를 호출합니다.
            userService.resetAllIsAttend();

            // Then: 각 유저의 isAttended 필드가 false로 변경되었는지 확인합니다.
            assertFalse(user1.getIsAttended(), "User1의 isAttended 필드는 false여야 합니다.");
            assertFalse(user2.getIsAttended(), "User2의 isAttended 필드는 false여야 합니다.");
            assertFalse(user3.getIsAttended(), "User3의 isAttended 필드는 false여야 합니다.");

            // resetIsAttend 메서드가 한 번 호출되었는지 검증
            verify(userRepository, times(1)).resetIsAttend();
        }

        @Test
        void 일주일_미출첵시_포인트_10퍼_감소_성공() {
            // Given: 초기 테스트 데이터 설정
            User user1 = createTestUser("example1@example.com", 100, LocalDate.now().minusDays(8), true);
            User user2 = createTestUser("example2@example.com", 200, LocalDate.now().minusDays(9), true);
            User user3 = createTestUser("example3@example.com", 300, LocalDate.now().minusDays(8), true);

            List<User> userList = Arrays.asList(user1, user2,user3);

            // 포인트 감소 및 출석일 업데이트 모의
            when(userRepository.decreaseAutomatically(any(LocalDate.class), any(LocalDate.class))).thenAnswer(invocation -> {
                userList.forEach(user -> {
                    Integer decreasedPoint = Math.max((int) (user.getPoint() * 0.9), 0);
                    ReflectionTestUtils.setField(user, "point", decreasedPoint);
                    ReflectionTestUtils.setField(user, "lastAttendDate", LocalDate.now());
                });
                return userList;
            });

            // Redis Mock 설정
            ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

            userService.decreasePointsAutomatically();

            assertEquals(90, user1.getPoint(), "User1의 포인트는 10% 감소하여 90이어야 합니다.");
            assertEquals(180, user2.getPoint(), "User2의 포인트는 10% 감소하여 180이어야 합니다.");
            assertEquals(270, user3.getPoint(),"User3의 포인트는 10% 감소하여 270이여야 합니다.");

            assertEquals(LocalDate.now(), user1.getLastAttendDate());
            assertEquals(LocalDate.now(), user2.getLastAttendDate());
            assertEquals(LocalDate.now(), user3.getLastAttendDate());

            verify(zSetOperations, times(1)).add(user1.checkRedisKey(user1), user1.getId(), user1.getPoint());
            verify(zSetOperations, times(1)).add(user2.checkRedisKey(user2), user2.getId(), user2.getPoint());
            verify(zSetOperations, times(1)).add(user3.checkRedisKey(user3), user3.getId(), user3.getPoint());
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
}
