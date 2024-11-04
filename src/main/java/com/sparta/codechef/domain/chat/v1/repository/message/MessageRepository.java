package com.sparta.codechef.domain.chat.v1.repository.message;

import com.sparta.codechef.domain.chat.v1.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, MessageQueryDslRepository {

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId AND m.isDeleted = false")
    List<Message> findAllMessagesByChatRoomId(Long chatRoomId);
}
