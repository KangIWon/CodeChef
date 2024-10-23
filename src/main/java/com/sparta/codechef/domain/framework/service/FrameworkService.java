package com.sparta.codechef.domain.framework.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.framework.dto.FrameworkRequest;
import com.sparta.codechef.domain.framework.entity.Framework;
import com.sparta.codechef.domain.framework.repository.FrameworkRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FrameworkService {

    private final FrameworkRepository frameworkRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createFramework(AuthUser authUser, FrameworkRequest frameworkRequest) {
        //유저 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        //유저가 어드민인지 체크
        if(user.getUserRole() == UserRole.ROLE_ADMIN) {
            Framework framework = Framework.builder().name(frameworkRequest.getName()).build();
            frameworkRepository.save(framework);
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);


        return null;
    }
    @Transactional
    public Void updateFramework(AuthUser authUser, FrameworkRequest frameworkRequest, Long id)
    {
        //유저 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        //프레임워크 조회
        Framework framework = frameworkRepository.findById(id).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK));

        //유저가 어드민인지 체크
        if(user.getUserRole() == UserRole.ROLE_ADMIN) {
            framework.update(frameworkRequest.getName());
            frameworkRepository.save(framework);
            return null;
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);

    }

    @Transactional
    public Void deleteFramework(AuthUser authUser, Long id) {
        //유저 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        //프레임워크 조회
        Framework framework = frameworkRepository.findById(id).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_FRAMEWORK));

        //유저가 어드민인지 체크
        if(user.getUserRole() == UserRole.ROLE_ADMIN) {
            frameworkRepository.delete(framework);
            return null;
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
    }


}
