package com.sparta.codechef.domain.user.controller;


import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.framework.dto.FrameworkRequest;
import com.sparta.codechef.domain.user.service.UserFrameworkService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserFrameworkController {

    private final UserFrameworkService userFrameworkService;

    @PostMapping("/users/frameworks/assign")
    public ApiResponse createUserFramework(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<FrameworkRequest> frameworkRequestList)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 프레임워크가 설정되었습니다.", userFrameworkService.createUserFramework(authUser, frameworkRequestList));
    }
    @GetMapping("/users/frameworks")
    public ApiResponse getUserFramework(@AuthenticationPrincipal AuthUser authUser)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 프레임워크 조회에 성공했습니다.", userFrameworkService.getUserFrameworks(authUser));
    }
    @PatchMapping("/users/frameworks")
    public ApiResponse updateUserFramework(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<FrameworkRequest> frameworkRequestList)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 프레임워크 수정에 성공했습니다.", userFrameworkService.updateUserFramework(authUser,frameworkRequestList));
    }


}
