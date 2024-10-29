package com.sparta.codechef.domain.chat.v1.repository.chat_room;


import com.sparta.codechef.domain.chat.v1.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomQueryDslRepository {

    @Query("SELECT c FROM ChatRoom c WHERE c.id = :chatRoomId AND c.user.id = :userId")
    Optional<ChatRoom> findByIdAndUser(Long chatRoomId, Long userId);

    @Query("SELECT c FROM ChatRoom c WHERE c.id = :chatRoomId AND c.isDeleted = false")
    Optional<ChatRoom> findExistChatRoomById(Long chatRoomId);
}
