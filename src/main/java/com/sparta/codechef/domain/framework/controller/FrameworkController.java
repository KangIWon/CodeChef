package com.sparta.codechef.domain.framework.controller;


import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.framework.dto.FrameworkRequest;
import com.sparta.codechef.domain.framework.service.FrameworkService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/frameworks")
public class FrameworkController {

    private final FrameworkService frameworkService;

    @PostMapping
    public ApiResponse createFramework(@AuthenticationPrincipal AuthUser authUser, FrameworkRequest frameworkRequest)
    {
       return ApiResponse.createSuccess(HttpStatus.OK.value(),"프레임워크가 등록되었습니다.",frameworkService.createFramework(authUser, frameworkRequest));
    }

    @PatchMapping("/{id}")
    public ApiResponse updateFramework(@AuthenticationPrincipal AuthUser authUser,FrameworkRequest frameworkRequest, @PathVariable Long id)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "프레임워크가 수정되었습니다.",frameworkService.updateFramework(authUser,frameworkRequest,id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteFramework(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "프레임워크가 삭제되었습니다.", frameworkService.deleteFramework(authUser,id));
    }





}
