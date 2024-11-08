package com.sparta.codechef.domain.chat.v2.service;

import com.sparta.codechef.domain.chat.v2.dto.UnsubscribeDTO;
import com.sparta.codechef.domain.chat.v2.dto.response.ChatUserResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import com.sparta.codechef.domain.chat.v2.repository.WSChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.sparta.codechef.domain.chat.v2.entity.MessageType.*;

@Service
@RequiredArgsConstructor
public class WSMessageService {

    private final WSChatRepository chatRepository;
    private static final String MESSAGE_KEY = "message";


    /**
     * 채팅방 구독
     * @param roomId : 채팅방 ID
     * @param chatUser : 입장한 유저 (id, email, role)
     * @return
     */
    public WSMessage subscribeChatRoom(Long roomId, WSChatUser chatUser) {
        boolean existChatRoom = this.chatRepository.existChatRoomByIdAndHostId(roomId, chatUser.getId());
        String email = chatUser.getEmail();

        if (existChatRoom) {
            return WSMessage.getMessage(roomId, email, CREATE);
        }

        return WSMessage.getMessage(roomId, email, IN);
    }


    /**
     * 이전 채팅 메세지 불러오기
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<WSMessage> getMessages(Long roomId) {
        return new ArrayList<>(this.chatRepository.getMessagesByRoomId(roomId));
    }

    /**
     * 채팅 메세지 전송
     * @param chatUser : 메세지 작성 유저 정보(id, email, role)
     * @param roomId : 채팅방 ID
     * @param content : 채팅 메세지
     * @return
     */
    public WSMessage sendMessage(WSChatUser chatUser, Long roomId, String content) {
        WSMessage message = new WSMessage(
                this.getMessageId(),
                roomId,
                new ChatUserResponse(chatUser),
                content
        );

        return this.chatRepository.saveMessage(message);
    }

    /**
     * 채팅방 퇴장(구독 취소)
     * @param roomId : 채팅방 ID
     * @param chatUser : 채팅 유저 (id, email, role)
     * @return
     */
    public List<WSMessage> unsubscribeChatRoom(Long roomId, WSChatUser chatUser, UnsubscribeDTO dto) {
        List<WSMessage> messageList = new ArrayList<>();
        Long nextHostId = dto.getNextHostId();

        if (nextHostId == null) {
            return messageList;
        }

        messageList.add(WSMessage.getMessage(roomId, chatUser.getEmail(), OUT));

        if (dto.isSuccess()) {
            String nextHostEmail = this.chatRepository.findEmailById(dto.getNextHostId());
            messageList.add(WSMessage.getMessage(roomId, nextHostEmail, NEW_HOST));
            return messageList;
        }

        return messageList;
    }

    private Long getMessageId() {
        return this.chatRepository.generateId(MESSAGE_KEY);
    }
}
