package com.sparta.codechef.domain.chat.v2.service;

import com.sparta.codechef.domain.chat.v2.dto.response.ChatUserResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import com.sparta.codechef.domain.chat.v2.repository.WSChatRepository;
import com.sparta.codechef.domain.chat.v2.repository.WSMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WSMessageService {

    private final WSChatRepository chatRepository;
    private final WSMessageRepository messageRepository;

    private static final String MESSAGE_KEY = "message";


    /**
     * 채팅방 생성
     * @param chatUser : 채팅방 생성 유저 정보(id, email, role)
     * @param roomId : 채팅방 Id
     * @return
     */
    public WSMessage createChatRoom(WSChatUser chatUser, Long roomId) {
        ChatUserResponse sender = new ChatUserResponse(chatUser);
        return new WSMessage(null, roomId, sender, "채팅방이 개설되었습니다.");
    }



    /**
     * 채팅방 입장
     * @param roomId : 채팅방 ID
     * @param email : 입장한 유저 이메일
     * @return
     */
    public List<WSMessage> subscribeChatRoom(Long roomId, String email) {
        String content = new StringBuffer(email)
                .append("님이 채팅방에 입장하셨습니다.")
                .toString();

        List<WSMessage> messageList = this.chatRepository.getMessagesByRoomId(roomId);
        messageList.add(new WSMessage(this.chatRepository.generateId(MESSAGE_KEY), roomId, null, content));
        return messageList;
    }

    /**
     * 채팅 메세지 전송
     * @param chatUser : 메세지 작성 유저 정보(id, email, role)
     * @param roomId : 채팅방 ID
     * @param content : 채팅 메세지
     * @return
     */
    public WSMessage sendMessage(WSChatUser chatUser, Long roomId, String content) {
        Long messageId = this.chatRepository.generateId(MESSAGE_KEY);
        ChatUserResponse sender = new ChatUserResponse(chatUser);

        WSMessage wsMessage = new WSMessage(messageId, roomId, sender, content);
        this.chatRepository.saveMessage(wsMessage);

        return wsMessage;
    }

    /**
     * 채팅방 퇴장(구독 취소)
     * @param email : 유저 이메일
     * @param roomId : 채팅방 ID
     * @return
     */
    public WSMessage unsubscribeChatRoom(String email, Long roomId) {
        String content = new StringBuffer(email).append("님이 채팅방에서 퇴장하셨습니다.").toString();
        return new WSMessage(null, roomId, null, content);
    }


    /**
     * 채팅방 방장 승계
     * @param roomId : 채팅방 ID
     * @param hostEmail : 새로운 방장 이메일
     * @return
     */
    public WSMessage seccessHost(Long roomId, String hostEmail) {
        String content = new StringBuffer(hostEmail).append("님이 새로운 방장이 되셨습니다.").toString();
        return new WSMessage(null, roomId, null, content);
    }
}
