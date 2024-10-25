package com.sparta.codechef.domain.auth.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.TokenType;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.auth.dto.AuthRequest;
import com.sparta.codechef.domain.auth.dto.AuthResponse;
import com.sparta.codechef.domain.user.dto.UserRequest;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import com.sparta.codechef.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    @Transactional
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

    @Transactional
    public AuthResponse.Login login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_CREDENTIALS));

        if (user.isBlocked()) {
            throw new ApiException(ErrorStatus.ACCOUNT_BLOCKED); // 계정이 차단된 경우 예외 발생
        }

        validateUserState(user);
        validatePasswordMatch(request.getPassword(), user.getPassword());
        validateAdminLogin(request, user);

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getUserRole());

        saveRefreshTokenInRedis(user.getId(), refreshToken);

        return new AuthResponse.Login(accessToken, refreshToken, user.getId(), user.getEmail(), user.getUserRole().name());
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

        private void saveRefreshTokenInRedis(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                TokenType.REFRESH.getLifeTime(),
                TimeUnit.MILLISECONDS
        );
    }

    public void logout(AuthUser user) {
        redisTemplate.delete(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getUserId());
    }

    @Transactional
    public void deleteUser(AuthUser user, UserRequest.Delete request) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new ApiException(ErrorStatus.INVALID_CREDENTIALS);
        }

        foundUser.isDelete();
        userRepository.save(foundUser);

        // refresh 토큰 삭제
        redisTemplate.delete(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getUserId());

    }

    @Transactional
    public void changePassword(AuthUser user, AuthRequest.ChangePassword changePasswordRequest) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

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

    @Transactional
    public void addWarningAndSetBlock(AuthUser user, Long userId) {
        User user1 = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        if (!user1.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
        }

        User user2 = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        user2.addWarningAndSetBlock();

        userRepository.save(user2);
    }

    public AuthResponse.GetMe getUserSensitiveInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        return new AuthResponse.GetMe(
                user.getId(),
                user.getEmail(),
                user.getUserRole(),
                user.getPersonalHistory(),
                user.getOrganization().name(),
                user.getWarning(),
                user.getPoint(),
                user.getIsAttended()
        );
    }

    public AuthResponse.GetOther getUserGeneralInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        return new AuthResponse.GetOther(
                user.getPersonalHistory(),
                user.getOrganization().name(),
                user.getPoint()
        );
    }

//    public AuthResponse.Reissue reissue(String refreshToken) {
//        if(refreshToken == null) {
//            return new AuthResponse.Reissue(ErrorStatus.NOT_FOUND_REFRESH_TOKEN));
//        }
//
//        // 프론트에서 붙여준 Bearer prefix 제거
//        try{
//            refreshToken = jwtUtil.substringToken(refreshToken);
//        } catch (NullPointerException e) {
//            return ResponseDto.of(HttpStatus.BAD_REQUEST, "잘못된 토큰 형식 입니다.", null);
//        }
//
//        // 리프레쉬 토큰인지 검사
//        String category = jwtUtil.getCategory(refreshToken);
//        if (!category.equals(TokenType.REFRESH.name())) {
//            return ResponseDto.of(HttpStatus.BAD_REQUEST, "리프레쉬 토큰이 아닙니다.");
//        }
//
//        // 토큰 만료 검사
//        try{
//            jwtUtil.isExpired(refreshToken);
//        } catch (ExpiredJwtException e) {
//            return ResponseDto.of(HttpStatus.UNAUTHORIZED, "만료된 리프레쉬 토큰입니다.", null);
//        }
//
//
//        String key = JwtUtil.REDIS_REFRESH_TOKEN_PREFIX  + jwtUtil.getUserId(refreshToken);
//        // 레디스에서 리프레쉬 토큰을 가져온다.
//        refreshToken = (String) redisTemplate.opsForValue().get(key);
//
//        if (refreshToken == null) {
//            return ResponseDto.of(HttpStatus.UNAUTHORIZED, "만료된 리프레쉬 토큰입니다.", null);
//        }
//
//        // redis에서 꺼내온 리프레쉬 토큰 prefix 제거
//        refreshToken = jwtUtil.substringToken(refreshToken);
//
//        // 검증이 통과되었다면 refresh 토큰으로 액세스 토큰을 발행해준다.
//        Claims claims = jwtUtil.extractClaims(refreshToken);
//        Long userId = Long.parseLong(claims.getSubject());
//        String email = claims.get("email", String.class);
//        UserRole userRole = UserRole.of(claims.get("userRole", String.class));
//
//        // 새 토큰 발급
//        String newAccessToken = jwtUtil.createAccessToken(userId, email, userRole);
//        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, userRole);
//
//        // TTL 새로해서
//        String userIdToString = String.valueOf(userId);
//        Long ttl = redisTemplate.getExpire(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + userIdToString, TimeUnit.MILLISECONDS);
//
//        if(ttl == null || ttl < 0) {
//            return ResponseDto.of(HttpStatus.UNAUTHORIZED, "만료된 리프레쉬 토큰입니다.", null);
//        }
//
//        redisTemplate.opsForValue().set(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX  + userIdToString, newRefreshToken, ttl, TimeUnit.MILLISECONDS);
//
//        Reissue reissue = new Reissue(newAccessToken, newRefreshToken);
//
//        return  ResponseDto.of(HttpStatus.OK, "", reissue);
//    }
}
