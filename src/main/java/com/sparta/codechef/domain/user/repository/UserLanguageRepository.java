package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
}
