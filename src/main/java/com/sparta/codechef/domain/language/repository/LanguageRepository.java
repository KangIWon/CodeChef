package com.sparta.codechef.domain.language.repository;

import com.sparta.codechef.domain.language.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
}
