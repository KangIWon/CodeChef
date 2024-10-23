package com.sparta.codechef.domain.chat.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.dto.response.MessageResponse;
import com.sparta.codechef.domain.chat.entity.ChatRoom;
import com.sparta.codechef.domain.chat.entity.Message;
import com.sparta.codechef.domain.chat.repository.chat_room.ChatRoomRepository;
import com.sparta.codechef.domain.chat.repository.message.MessageRepository;
import com.sparta.codechef.domain.user.dto.response.UserResponse;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 채팅 메세지 전송
     *
     * @param userId     : 유저 id
     * @param chatRoomId : 채팅방 id
     * @param message    : 채팅 메세지
     * @return
     */
    @Transactional
    public MessageResponse sendMessage(Long userId, Long chatRoomId, String message) {
        ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        Message msg = Message.builder()
                .message(message)
                .chatRoom(chatRoom)
                .user(user)
                .build();

        Message savedMessage = this.messageRepository.save(msg);

        return new MessageResponse(
                savedMessage.getId(),
                savedMessage.getMessage(),
                new UserResponse(user.getId(), user.getEmail()),
                savedMessage.getCreatedAt()
                );
    }

    /**
     * 채팅방 메세지 다건 조회
     * @param userId : 유저 ID
     * @param chatRoomId : 채팅방 ID
     * @return
     */
    public List<MessageResponse> getMessages(Long userId, Long chatRoomId) {
        boolean inChatRoom = this.userRepository.existsUserByIdAndChatRoomId(userId, chatRoomId);

        if (!inChatRoom) {
            throw new ApiException(ErrorStatus.NOT_IN_CHATROOM);
        }

        return this.messageRepository.findAllByChatRoomId(chatRoomId);
    }
}
