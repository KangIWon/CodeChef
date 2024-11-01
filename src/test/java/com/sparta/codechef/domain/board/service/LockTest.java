package com.sparta.codechef.domain.board.service;

import com.sparta.codechef.domain.event.service.EventTestService;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class LockTest {

    @Autowired
    private EventTestService eventService; // eventPoints 메서드가 포함된 서비스

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("락없이 테스트")
    public void testWithoutLock() throws InterruptedException {
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();

        // given
        Long id = 1L;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.eventPointsDirectDB(id);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        User updatedUser = userRepository.findById(1L).orElseThrow();
        System.out.println("저장된 포인트: " + updatedUser.getPoint());

        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (DB 바로 저장): " + (endTime - startTime) + " ms");
    }

    @Test
    @DisplayName("낙관락 테스트")
    public void testOptimistic() throws InterruptedException {

        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();

        // given
        Long id = 2L;
        AtomicInteger totalFailures = new AtomicInteger(0);

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    int attempts = eventService.eventPointsOptimisticLock(id);
                    totalFailures.addAndGet(attempts);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("Total attempts: " + totalFailures.get());
        System.out.println("Final point count (optimistic): " + updatedUser.getPoint());

        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Optimistic Lock): " + (endTime - startTime) + " ms");
    }

    @Test
    @DisplayName("비관락 테스트")
    public void testPessimistic() throws InterruptedException {
        // Final like count (pessimistic): 1300000
        //Execution time (Pessimistic Lock): 7452 ms

        long startTime = System.currentTimeMillis();

        // given
        Long id = 3L;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.eventPointsPessimisticLock(id);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("Final like count (pessimistic): " + updatedUser.getPoint());

        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Pessimistic Lock): " + (endTime - startTime) + " ms");
    }


    @Test
    @DisplayName("루아 테스트")
    public void 루아_스크립트_사용() throws InterruptedException {
        // User points after event: 100000
        // Execution time (Lua Script): 3191 ms

        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();

        Long id = 4L;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.luaScriptEventPoints(id);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("User points after event: " + updatedUser.getPoint());

        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Lua Script): " + (endTime - startTime) + " ms");
    }


    @Test
    @DisplayName("분산락 테스트")
    public void 분산락_사용() throws InterruptedException {
        // User points after event: 100000
        // Execution time (Distributed Lock): 4558 ms

        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();

        Long id = 5L;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                try {
                    eventService.eventPoints(id);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("User points after event: " + updatedUser.getPoint());

        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Distributed Lock): " + (endTime - startTime) + " ms");
    }
}
