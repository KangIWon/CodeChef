package com.sparta.codechef.domain.chat.v1.repository.message;

import com.sparta.codechef.domain.chat.v1.dto.response.MessageResponse;

import java.util.List;

public interface MessageQueryDslRepository {

    List<MessageResponse> findAllByChatRoomId(Long chatRoomId);
}
