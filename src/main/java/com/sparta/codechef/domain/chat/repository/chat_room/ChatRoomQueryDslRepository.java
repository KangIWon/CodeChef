package com.sparta.codechef.domain.chat.repository.chat_room;

import com.sparta.codechef.domain.chat.dto.response.ChatRoomGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomQueryDslRepository {

    Page<ChatRoomGetResponse> findAllChatRoom(Pageable pageable);
}
