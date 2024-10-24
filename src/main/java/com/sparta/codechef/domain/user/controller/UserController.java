package com.sparta.codechef.domain.user.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.user.dto.response.UserPoint;
import com.sparta.codechef.domain.user.service.UserService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //출석체크 시 포인트 지급
    @PostMapping("/users/points/credit")
    ApiResponse<Void> creditPoints(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok("포인트 지급이 완료 되었습니다", userService.creditPoints(authUser));
    }

    @GetMapping("/users/points")
    ApiResponse<UserPoint> getPoints(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok("포인트 조회가 되었습니다", userService.getUserPoint(authUser));
    }
}
