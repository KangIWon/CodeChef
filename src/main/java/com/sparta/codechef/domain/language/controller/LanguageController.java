package com.sparta.codechef.domain.language.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.framework.dto.FrameworkRequest;
import com.sparta.codechef.domain.framework.service.FrameworkService;
import com.sparta.codechef.domain.language.dto.LanguageRequest;
import com.sparta.codechef.domain.language.service.LanguageService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/languages")
public class LanguageController {
    private final LanguageService languageService;

    @PostMapping
    public ApiResponse createLanguage(@AuthenticationPrincipal AuthUser authUser, @RequestBody LanguageRequest languageRequest)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(),"언어가 등록되었습니다.",languageService.createLanguage(authUser, languageRequest));
    }

    @PatchMapping("/{id}")
    public ApiResponse updateLanguage(@AuthenticationPrincipal AuthUser authUser, @RequestBody LanguageRequest languageRequest, @PathVariable Long id)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "언어가 수정되었습니다.",languageService.updateLanguage(authUser,languageRequest,id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteLanguage(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "언어가 삭제되었습니다.", languageService.deleteLanguage(authUser,id));
    }
}
