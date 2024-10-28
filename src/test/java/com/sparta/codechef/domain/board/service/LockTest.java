//package com.sparta.codechef.domain.board.service;
//
//import com.sparta.codechef.common.enums.Organization;
//import com.sparta.codechef.common.enums.UserRole;
//import com.sparta.codechef.domain.chat.entity.ChatRoom;
//import com.sparta.codechef.domain.comment.service.CommentService;
//import com.sparta.codechef.domain.user.entity.User;
//import com.sparta.codechef.domain.user.repository.UserRepository;
//import com.sparta.codechef.domain.user.service.UserService;
//import com.sparta.codechef.security.AuthUser;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@SpringBootTest
//public class LockTest {
//
//    @Autowired
//    private UserService yourService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CommentService commentService;
//
//
//    @Test
//    void testRetryableCreditPoints() throws Exception {
//        // 테스트할 사용자 추가
//        AuthUser authUser = new AuthUser(1L, "123", UserRole.ROLE_USER);
//
//        // 동시 실행할 스레드 수
//        int threadCount = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch readyLatch = new CountDownLatch(1); // 모든 스레드 대기 후 시작
//        CountDownLatch finishLatch = new CountDownLatch(100); // 작업 완료 대기
//
//        AtomicInteger totalFailures = new AtomicInteger(0);
//
//        long startTime = System.currentTimeMillis();
//
//        // 여러 스레드가 동시에 adoptedComment 메서드를 호출
//        for (int i = 0; i < 100; i++) {
//            executor.execute(() -> {
//                try {
//                    readyLatch.await(); // 모든 스레드가 준비될 때까지 대기
//                    commentService.adoptedComment(authUser, 3L, 7L);
//                } catch (ObjectOptimisticLockingFailureException e) {
//                    totalFailures.incrementAndGet(); // 낙관적 락 예외 발생 횟수 기록
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    finishLatch.countDown(); // 작업 완료 시 카운트 감소
//                }
//            });
//        }
//
//        readyLatch.countDown(); // 모든 스레드가 동시에 시작
//        finishLatch.await(); // 모든 작업이 완료될 때까지 대기
//        executor.shutdown();
//
//        long endTime = System.currentTimeMillis();
//
//        // 결과 확인
//        User updatedUser = userRepository.findById(authUser.getUserId()).orElseThrow();
//        System.out.println("Total failures due to Optimistic Locking: " + totalFailures.get());
//        System.out.println("Final points: " + updatedUser.getPoint());
//
//        // 실행 시간 출력
//        System.out.println("Execution time: " + (endTime - startTime) + " ms");
//    }
//
//}
