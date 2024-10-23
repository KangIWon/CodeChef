package com.sparta.codechef.domain.user.service;

import com.sparta.codechef.domain.user.dto.UserRequest;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import com.sparta.codechef.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

//    public void changePassword(Long userId, String oldPassword, String newPassword) {
//
//    }
//
//    public void deleteUser(Long userId, AuthUser authUser, UserRequest.Delete request) {
//        if(userId != authUser.getUserId()) {
//            //throw new AccessDeniedException("탈퇴 권한이 없습니다.");
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow();//UserNotFoundException::new);
//
//        if(passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            //throw new AccessDeniedException("비밀번호가 다릅니다.");
//        }
//
//        if(user.getIsDeleted()) {
//            //throw new InvalidRequestException("이미 탈퇴한 유저입니다.");
//        }
//
//        user.isDelete();
//        userRepository.save(user);
//
//        // refresh 토큰 삭제
//        redisTemplate.delete(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + authUser.getUserId());
//    }
}
