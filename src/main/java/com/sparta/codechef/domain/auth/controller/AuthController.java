package com.sparta.codechef.domain.auth.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.auth.dto.AuthRequest;
import com.sparta.codechef.domain.auth.dto.AuthResponse;
import com.sparta.codechef.domain.auth.service.AuthService;
import com.sparta.codechef.domain.user.dto.UserRequest;
import com.sparta.codechef.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<AuthResponse.Signup> signup(@Valid @RequestBody AuthRequest.Signup authRequest) {
        AuthResponse.Signup response = authService.signup(authRequest);
        return ApiResponse.ok("회원가입이 완료되었습니다.", response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse.Login> login(@Valid @RequestBody AuthRequest.Login authRequest) {
        AuthResponse.Login response = authService.login(authRequest);
        return ApiResponse.ok("로그인 성공", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AuthUser user) {
        authService.logout(user);
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "로그아웃되었습니다.", null);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal AuthUser user, @RequestBody UserRequest.Delete request) {
        authService.deleteUser(user, request);
        return ApiResponse.ok( "회원 탈퇴 완료", null);
    }

    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal AuthUser user, @RequestBody AuthRequest.ChangePassword changePasswordRequest) {
        authService.changePassword(user, changePasswordRequest);
        return ApiResponse.ok( "비밀번호 변경 완료", null);
    }

    @GetMapping("/email/check")
    public ApiResponse<AuthResponse.DuplicateCheck> checkEmail(@RequestBody AuthRequest.CheckEmail request) {
        AuthResponse.DuplicateCheck response = authService.checkEmail(request);
        return ApiResponse.ok( "이메일 중복 확인", response);
    }

    @PatchMapping("/warning/{userId}")
    public ApiResponse<Void> addWarningAndSetBlock(@AuthenticationPrincipal AuthUser user, @PathVariable Long userId) {
        authService.addWarningAndSetBlock(user, userId);
        return ApiResponse.ok( "경고 추가 완료", null);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<?> getUserInfo(@AuthenticationPrincipal AuthUser user, @PathVariable Long userId) {
        if (user.getUserId().equals(userId)) {
            // 본인일 경우
            AuthResponse.getMe response = authService.getUserSensitiveInfo(userId);
            return ApiResponse.ok( "사용자 민감정보 조회 성공", response);
        } else {
            // 다른 사용자일 경우
            AuthResponse.getOther response = authService.getUserGeneralInfo(userId);
            return ApiResponse.ok( "사용자 일반정보 조회 성공", response);
        }
    }
}
