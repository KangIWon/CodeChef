package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.UserFramework;
import com.sparta.codechef.domain.user.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
    Optional<UserLanguage> findByUserIdAndLanguageId(Long userId, Long languageId);
    Optional<List<UserLanguage>> findAllByUserId(Long userId);
}
