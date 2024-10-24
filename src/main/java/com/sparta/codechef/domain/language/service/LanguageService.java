package com.sparta.codechef.domain.language.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.language.dto.LanguageRequest;
import com.sparta.codechef.domain.language.entity.Language;
import com.sparta.codechef.domain.language.repository.LanguageRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LanguageService {

    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createLanguage(AuthUser authUser, LanguageRequest languageRequest) {
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        if(user.getUserRole() == UserRole.ROLE_ADMIN)
        {
            Language language = Language.builder().name(languageRequest.getName()).build();
            languageRepository.save(language);
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);

        return null;
    }
    @Transactional
    public Void updateLanguage(AuthUser authUser, LanguageRequest languageRequest, Long id) {
        //유저 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        //프레임워크 조회
        Language language = languageRepository.findById(id).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_LANGUAGE));

        //유저가 어드민인지 체크
        if(user.getUserRole() == UserRole.ROLE_ADMIN) {
            language.update(languageRequest.getName());
            languageRepository.save(language);
            return null;
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
    }


    @Transactional
    public Void deleteLanguage(AuthUser authUser, Long id) {
        //유저 조회
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        //프레임워크 조회
        Language language = languageRepository.findById(id).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_LANGUAGE));

        //유저가 어드민인지 체크
        if(user.getUserRole() == UserRole.ROLE_ADMIN) {
            languageRepository.delete(language);
            return null;
        }
        else
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
    }
}
