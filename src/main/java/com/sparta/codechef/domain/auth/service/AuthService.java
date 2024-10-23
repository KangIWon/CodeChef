package com.sparta.codechef.domain.auth.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.auth.dto.AuthRequest;
import com.sparta.codechef.domain.auth.dto.AuthResponse;
import com.sparta.codechef.domain.user.dto.UserRequest;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import com.sparta.codechef.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    public AuthResponse.Signup signup(AuthRequest.Signup request) {
        validatePassword(request.getPassword());

        validateAdminRole(request);

        String email = request.getEmail();
        checkEmailDuplicate(email);

        User user = registerUser(request);
        return new AuthResponse.Signup(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getUserRole(),
                user.getPersonalHistory(),
                user.getOrganization().name());
    }

    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$")) {
            throw new ApiException(ErrorStatus.INVALID_REQUEST);
        }
    }

    private void validateAdminRole(AuthRequest.Signup request) {
        if (request.getUserRole() == UserRole.ROLE_ADMIN) {
            if (!StringUtils.hasText(request.getAdminToken()) || !request.getAdminToken().equals(adminToken)) {
                throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
            }
        }
    }

    private void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorStatus.DUPLICATE_EMAIL);
        }
    }

    private User registerUser(AuthRequest.Signup request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getUserRole())
                .personalHistory(request.getPersonalHistory())
                .organization(Organization.valueOf(request.getOrganization()))
                .isDeleted(false)
                .isAttended(false)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse.Login login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_CREDENTIALS));

        validateUserState(user);
        validatePasswordMatch(request.getPassword(), user.getPassword());
        validateAdminLogin(request, user);

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        return new AuthResponse.Login(bearerToken, user.getId(), user.getEmail(), user.getUserRole().name());
    }

    private void validateUserState(User user) {
        if (user.getIsDeleted()) {
            throw new ApiException(ErrorStatus.USER_DELETED);
        }
    }

    private void validatePasswordMatch(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ApiException(ErrorStatus.INVALID_CREDENTIALS);
        }
    }

    private void validateAdminLogin(AuthRequest.Login request, User user) {
        if (user.getUserRole() == UserRole.ROLE_ADMIN) {
            if (!StringUtils.hasText(request.getAdminToken()) || !request.getAdminToken().equals(adminToken)) {
                throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
            }
        }
    }

    public void logout(AuthUser user) {
//        redisTemplate.delete(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getUserId());
        //redisTemplate.delete(JwtUtil. + user.getUserId());
    }

    public void deleteUser(AuthUser user, UserRequest.Delete request) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new ApiException(ErrorStatus.INVALID_CREDENTIALS);
        }

        foundUser.isDelete();
        userRepository.save(foundUser);

        // refresh 토큰 삭제
//        redisTemplate.delete(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getUserId());

    }

    public void changePassword(AuthUser user, AuthRequest.ChangePassword changePasswordRequest) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), foundUser.getPassword())) {
            throw new ApiException(ErrorStatus.INVALID_CREDENTIALS);
        }

        foundUser.changePassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(foundUser);
    }

    public AuthResponse.DuplicateCheck checkEmail(AuthRequest.CheckEmail request) {
        boolean isDuplicate = userRepository.existsByEmail(request.getEmail());
        return new AuthResponse.DuplicateCheck(isDuplicate);
    }

//    public void addWarningAndSetBlock(AuthUser user, Long userId) {
//        User user1 = userRepository.findById(user.getUserId())
//                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));
//
//        if (!user1.getUserRole().equals(UserRole.ROLE_ADMIN)) {
//            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
//        }
//
//        User user2 = userRepository.findById(userId)
//                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));
//
//        user2.addWarningAndSetBlock(1);
//    }
}
