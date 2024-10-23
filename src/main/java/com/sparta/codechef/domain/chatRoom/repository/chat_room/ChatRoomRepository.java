package com.sparta.codechef.domain.chatRoom.repository.chat_room;

import com.sparta.codechef.domain.chatRoom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomQueryDslRepository {
}
