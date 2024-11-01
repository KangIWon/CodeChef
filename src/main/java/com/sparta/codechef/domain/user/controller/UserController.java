package com.sparta.codechef.domain.user.controller;


import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.user.dto.response.UserPoint;
import com.sparta.codechef.domain.user.dto.response.UserRankingTop3;
import com.sparta.codechef.domain.user.service.RankingService;
import com.sparta.codechef.domain.user.service.UserService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RankingService rankingService;

    //출석체크 시 포인트 지급
    @PostMapping("/users/points/credit")
    ApiResponse<Void> creditPoints(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok("포인트 지급이 완료 되었습니다", userService.creditPoints(authUser));
    }

    @GetMapping("/users/points")
    ApiResponse<UserPoint> getPoints(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok("포인트 조회가 되었습니다", userService.getUserPoint(authUser));
    }

    @GetMapping("/ranking/employed/last-month")
    public ApiResponse<List<UserRankingTop3>> getRankingTop3EmployedLastMonth() {
        return ApiResponse.ok("지난달 현직자 포인트 랭킹 top3가 조회되었습니다.", rankingService.getRankingTop3EmployedLastMonth());
    }

    @GetMapping("/ranking/unemployed/last-month")
    public ApiResponse<List<UserRankingTop3>> getRankingTop3UnemployedLastMonth() {
        return ApiResponse.ok("지난달 비현직자 포인트 랭킹 top3가 조회되었습니다.", rankingService.getRankingTop3UnemployedLastMonth());
    }

    @GetMapping("/ranking/employed/real-time")
    public ApiResponse<List<UserRankingTop3>> getRankingTop3EmployedRealTime() {
        return ApiResponse.ok("실시간 현직자 포인트 랭킹 top3가 조회되었습니다.",rankingService.getRankingTop3EmployedRealTime());
    }

    @GetMapping("/ranking/unemployed/real-time")
    public ApiResponse<List<UserRankingTop3>> getRankingTop3UnemployedRealTime() {
        return ApiResponse.ok("실시간 비현직자 포인트 랭킹 top3가 조회되었습니다.",rankingService.getRankingTop3UnemployedRealTime());
    }
}
