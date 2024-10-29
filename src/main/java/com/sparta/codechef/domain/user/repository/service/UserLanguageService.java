package com.sparta.codechef.domain.user.repository.service;


import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.language.dto.LanguageRequest;
import com.sparta.codechef.domain.language.dto.LanguageResponse;
import com.sparta.codechef.domain.language.entity.Language;
import com.sparta.codechef.domain.language.repository.LanguageRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.entity.UserFramework;
import com.sparta.codechef.domain.user.entity.UserLanguage;
import com.sparta.codechef.domain.user.repository.UserLanguageRepository;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserLanguageService {

    private final UserLanguageRepository userLanguageRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    @Transactional
    public Void createUserLanguage(AuthUser authUser, List<LanguageRequest> languageRequestlist){
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER));

        for(LanguageRequest languageRequest : languageRequestlist)
        {
            Language language = languageRepository.findByName(languageRequest.getName()).orElseThrow(()->
                    new ApiException(ErrorStatus.NOT_FOUND_LANGUAGE));

            Optional<UserLanguage> existingUserLanguage = userLanguageRepository.findByUserIdAndLanguageId(user.getId(), language.getId());

            if(!existingUserLanguage.isPresent())
            {
                UserLanguage userLanguage = new UserLanguage(user,language);
                userLanguageRepository.save(userLanguage);

            }
            else
                throw new ApiException(ErrorStatus.ALREADY_ASSIGNED_USER_LANGUAGE);

        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<LanguageResponse> getUserLanguages(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER));

        List<UserLanguage> userLanguages = userLanguageRepository.findAllByUserId(user.getId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER_LANGUAGE));

        List<LanguageResponse> languageResponseList = userLanguages.stream()
                .map(userLanguage ->{
                    Language language = languageRepository.findById(userLanguage.getLanguage().getId())
                            .orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_LANGUAGE));

                        return new LanguageResponse(language.getName());
                    }).toList();
        return languageResponseList;
    }

    @Transactional
    public Object updateUserLanguage(AuthUser authUser, List<LanguageRequest> languageRequestlist) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        List<UserLanguage> userLanguageList = userLanguageRepository.findAllByUserId(user.getId())
                .orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER_LANGUAGE));

        Map<String, Language> languageMap = languageRequestlist.stream()
                .map(LanguageRequest::getName)
                .collect(Collectors.toMap(
                        name -> name,
                        name -> languageRepository.findByName(name)
                                .orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_LANGUAGE))
                ));

        for(UserLanguage userLanguage : userLanguageList) {
            for(LanguageRequest languageRequest : languageRequestlist)
            {
                if(languageMap.containsKey(languageRequest.getName())) {
                    Language newlanguage = languageMap.get(languageRequest.getName());
                    userLanguage.update(newlanguage);
                    userLanguageRepository.save(userLanguage);
                }
            }
        }

        return null;
    }
}
