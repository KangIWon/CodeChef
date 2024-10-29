package com.sparta.codechef.domain.chat.v1.repository.chat_room;

import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomQueryDslRepository {

    Page<ChatRoomGetResponse> findAllChatRoom(Pageable pageable);

    boolean existsByIdAndUserId(Long chatRoomId, Long userId);
}
