package com.sparta.codechef.domain.user.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.dto.response.UserRankingTop3;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {


    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper jacksonObjectMapper;


    @PostConstruct
    public void init() {
        updateUserPoint();
    }


    public void updateUserPoint() {
        List<User> all = userRepository.findAll();
        for (User user : all) {
            String redisKey = user.checkRedisKey(user);
            redisTemplate.opsForZSet().add(redisKey, user.getId(), user.getPoint());
        }
    }

    @Scheduled(cron = "0 0 0 L * ?")
    @Transactional// 매달 마지막 날 00:00에 실행
    public void saveRankingTop3LastMonth() {
        saveLastMonthEmployedRanking();     // 저번달 현업자 랭킹 저장
        saveLastMonthUnEmployedRanking();   // 저번달 비현업자 랭킹 저장
        userRepository.resetUserPoint();    // 유저 포인트 리셋
        updateUserPoint();                  // 리셋한 유저 포인트 다시 레디스에 올려서 레디스에도 초기화
    }

    private void saveLastMonthEmployedRanking() {
        String realTimeKey = "employed:ranking:realTime";
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> top3Users = zSetOps.reverseRangeWithScores(
                realTimeKey, 0, 2);

        if (top3Users == null || top3Users.isEmpty()) {
            return;
        }
        List<UserRankingTop3> top3LastMonth = getUserRankingTop3s(realTimeKey);
        String lastMonthKey = "employed:ranking:lastMonth:" + getLastMonthString();
        try {
            String json = jacksonObjectMapper.writeValueAsString(top3LastMonth);
            redisTemplate.opsForValue().set(lastMonthKey, json);
        } catch (IOException ioException) {
            throw new ApiException(ErrorStatus.JSON_CHANGE_FAILED);
        }
    }

    private void saveLastMonthUnEmployedRanking() {
        String realTimeKey = "unemployed:ranking:realTime";
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> top3Users = zSetOps.reverseRangeWithScores(
                realTimeKey, 0, 2);
        if (top3Users == null || top3Users.isEmpty()) {
            return;
        }
        List<UserRankingTop3> top3LastMonth = getUserRankingTop3s(realTimeKey);
        String lastMonthKey = "unemployed:ranking:lastMonth:" + getLastMonthString();
        try {
            String json = jacksonObjectMapper.writeValueAsString(top3LastMonth);
            redisTemplate.opsForValue().set(lastMonthKey, json);
        } catch (IOException ioException) {
            throw new ApiException(ErrorStatus.JSON_CHANGE_FAILED);
        }
    }

    // 저번달 현업자 랭킹
    public List<UserRankingTop3> getRankingTop3EmployedLastMonth() {
        String key = "employed:ranking:lastMonth:" + getLastMonthString();
        String cachedData = (String) redisTemplate.opsForValue().get(key);
        List<UserRankingTop3> userRankingTop3s;
        try {
            userRankingTop3s = jacksonObjectMapper.readValue(cachedData,
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return userRankingTop3s;
    }

    // 저번달 비현업자 랭킹
    public List<UserRankingTop3> getRankingTop3UnemployedLastMonth() {
        String key = "unemployed:ranking:lastMonth:" + getLastMonthString();
        String cachedData = (String) redisTemplate.opsForValue().get(key);
        List<UserRankingTop3> userRankingTop3s;
        try {
            userRankingTop3s = jacksonObjectMapper.readValue(cachedData,
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return userRankingTop3s;
    }

    // 현업자 랭킹 현업자 랭킹(실시간)
    @Cacheable(value = "employedRanking", key = "'realTime'")
    public List<UserRankingTop3> getRankingTop3EmployedRealTime() {
        String key = "employed:ranking:realTime";
        return getUserRankingTop3s(key);
    }

    // 비현업자 랭킹 현업자 랭킹(실시간)
    @Cacheable(value = "unemployedRanking", key = "'realTime'")
    public List<UserRankingTop3> getRankingTop3UnemployedRealTime() {
        String key = "unemployed:ranking:realTime";
        return getUserRankingTop3s(key);
    }

    // 실시간 현업자 랭킹 & 비현업자 랭킹 메서드
    private List<UserRankingTop3> getUserRankingTop3s(String key) {
        ZSetOperations<String, Object> stringObjectZSetOperations = redisTemplate.opsForZSet();
        Set<TypedTuple<Object>> typedTuples = stringObjectZSetOperations.reverseRangeWithScores(key,
                0, 2);

        if (typedTuples == null || typedTuples.isEmpty()) {
            return Collections.emptyList(); // 랭킹에 데이터가 없는 경우 빈 리스트 반환
        }
        return typedTuples.stream().map(tuple -> {
            Long userId = null;

            if (tuple.getValue() instanceof Long) {
                userId = (Long) tuple.getValue();
            } else if (tuple.getValue() instanceof Integer) {
                userId = ((Integer) tuple.getValue()).longValue(); // Integer일 경우 Long으로 변환
            }

            return new UserRankingTop3(
                    userId,
                    tuple.getScore().intValue()
            );
        }).collect(Collectors.toList());
    }

    private String getLastMonthString() {
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);
        return lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}

