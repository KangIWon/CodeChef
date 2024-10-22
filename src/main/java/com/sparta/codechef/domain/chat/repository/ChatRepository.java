package com.sparta.codechef.domain.chat.repository;

import com.sparta.codechef.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, ChatQueryDslRepository {
}
