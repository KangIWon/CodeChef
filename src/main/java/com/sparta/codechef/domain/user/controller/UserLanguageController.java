package com.sparta.codechef.domain.user.controller;


import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.language.dto.LanguageRequest;
import com.sparta.codechef.domain.user.repository.service.UserLanguageService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserLanguageController {

    private final UserLanguageService userLanguageService;

    @PostMapping("/users/languages/assign")
    public ApiResponse createUserLanguage(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<LanguageRequest> languageRequestlist)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 언어가 설정되었습니다.", userLanguageService.createUserLanguage(authUser, languageRequestlist));
    }
    @GetMapping("/users/languages")
    public ApiResponse getUserLanguages(@AuthenticationPrincipal AuthUser authUser)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 언어 조회에 성공했습니다.", userLanguageService.getUserLanguages(authUser));
    }
    @PatchMapping("/users/languages")
    public ApiResponse deleteUserLanguage(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<LanguageRequest> languageRequestlist)
    {
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "유저 언어 수정에 성공하였습니다.", userLanguageService.updateUserLanguage(authUser, languageRequestlist));
    }






}
