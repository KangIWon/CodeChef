package com.sparta.codechef.domain.chatRoom.repository.message;

import com.sparta.codechef.domain.chatRoom.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, MessageQueryDslRepository {
}
