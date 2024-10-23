package com.sparta.codechef.domain.chat.service;

import com.sparta.codechef.domain.chat.dto.response.MessageResponse;
import com.sparta.codechef.domain.chat.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    /**
     * 채팅 메세지 전송
     * @param message : 채팅 글
     * @return
     */
    public MessageResponse sendMessage(String message) {
        return null;
    }

    /**
     * 채팅방 메세지 다건 조회
     * @param chatRoomId : 채팅방 ID
     * @return
     */
    public List<MessageResponse> getMessages(Long chatRoomId) {
        return null;
    }
}
