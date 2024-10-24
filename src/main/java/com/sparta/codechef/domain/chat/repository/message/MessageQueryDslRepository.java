package com.sparta.codechef.domain.chat.repository.message;

import com.sparta.codechef.domain.chat.dto.response.MessageResponse;

import java.util.List;

public interface MessageQueryDslRepository {

    List<MessageResponse> findAllByChatRoomId(Long chatRoomId);
}
