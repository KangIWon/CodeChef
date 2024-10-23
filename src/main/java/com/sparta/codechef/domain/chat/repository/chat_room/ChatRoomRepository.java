package com.sparta.codechef.domain.chat.repository.chat_room;


import com.sparta.codechef.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomQueryDslRepository {
}
