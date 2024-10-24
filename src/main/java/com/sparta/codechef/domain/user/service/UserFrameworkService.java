package com.sparta.codechef.domain.user.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.framework.dto.FrameworkRequest;
import com.sparta.codechef.domain.framework.dto.FrameworkResponse;
import com.sparta.codechef.domain.framework.entity.Framework;
import com.sparta.codechef.domain.framework.repository.FrameworkRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.entity.UserFramework;
import com.sparta.codechef.domain.user.repository.UserFrameworkRepository;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserFrameworkService {

    private final UserFrameworkRepository userFrameworkRepository;
    private final UserRepository userRepository;
    private final FrameworkRepository frameworkRepository;

    @Transactional
    public Void createUserFramework(AuthUser authUser, List<FrameworkRequest> frameworkRequestList) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER));

        for(FrameworkRequest frameworkRequest : frameworkRequestList)
        {
            Framework framework = frameworkRepository.findByName(frameworkRequest.getName()).orElseThrow(()->
                    new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK));
            // 중복 체크: 이미 User와 Framework 조합이 존재하는지 확인
            Optional<UserFramework> existingUserFramework = userFrameworkRepository.findByUserIdAndFrameworkId(user.getId(), framework.getId());

            // 존재하지 않을 때만 저장
            if (!existingUserFramework.isPresent()) {
                UserFramework userFramework = new UserFramework(user, framework);
                userFrameworkRepository.save(userFramework);
            }
            else {
                throw new ApiException(ErrorStatus.ALREADY_ASSIGNED_USER_FRAMEWORK);
            }
        }

        return null;
    }
    @Transactional(readOnly = true)
    public List<FrameworkResponse> getUserFrameworks(AuthUser authUser) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK));
        // 사용자의 UserFramework 목록을 가져오기
        List<UserFramework> userFrameworks = userFrameworkRepository.findAllByUserId(user.getId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER_FRAMEWORK));

        // 프레임워크 정보 추출 및 DTO 변환
        List<FrameworkResponse> frameworkResponseList = userFrameworks.stream()
                .map(userFramework -> {
                    Framework framework = frameworkRepository.findById(userFramework.getFramework().getId())
                            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK));

                    return new FrameworkResponse(framework.getName());
                }).toList();

        return frameworkResponseList;
    }
    @Transactional
    public Void updateUserFramework(AuthUser authUser, List<FrameworkRequest> frameworkRequestList) {

        // 사용자 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        // 현재 사용자의 UserFramework 목록 가져오기
        List<UserFramework> userFrameworkList = userFrameworkRepository.findAllByUserId(user.getId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER_FRAMEWORK));

        // 요청받은 FrameworkRequestList에서 프레임워크 이름을 기준으로 미리 조회하여 맵핑
        Map<String, Framework> frameworkMap = frameworkRequestList.stream()
                .map(FrameworkRequest::getName)  // 이름만 추출
                .collect(Collectors.toMap(
                        name -> name,  // 이름을 키로 사용
                        name -> frameworkRepository.findByName(name)
                                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK)) // 프레임워크 조회
                ));

        // UserFramework 업데이트
        for (UserFramework userFramework : userFrameworkList) {
            // 요청된 프레임워크 목록과 매칭
            for (FrameworkRequest frameworkRequest : frameworkRequestList) {
                if (frameworkMap.containsKey(frameworkRequest.getName())) {
                    // 매칭된 프레임워크로 업데이트
                    Framework newFramework = frameworkMap.get(frameworkRequest.getName());
                    userFramework.update(newFramework);
                    userFrameworkRepository.save(userFramework); // 업데이트된 UserFramework 저장
                }
            }
        }

        return null;
    }
}
