package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
}
