package com.sparta.codechef.domain.chat.v2.controller;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import com.sparta.codechef.domain.chat.v2.service.WSMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j(topic = "WSController")
@RestController
@RequiredArgsConstructor
public class WSController {

    private final WSMessageService wsMessageService;

    /**
     * 채팅방 생성
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @SubscribeMapping("/chat-room/{roomId}/create")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleCreateChatRoom(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("채팅방 개설 : chat-room-{}, host : {}", roomId, chatUser.getId());

        return this.wsMessageService.createChatRoom(chatUser, roomId);
    }



    /**
     * 채팅방 입장 (구독 요청)
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return 입장 안내 메세지
     */
    @SubscribeMapping("/chat-room/{roomId}/enter")
    @SendTo("/topic/chat-room/{roomId}")
    public List<WSMessage> handleSubscription(@DestinationVariable Long roomId,
                                              SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("채팅방 입장 : chat-room-{}, host : {}", roomId, chatUser.getId());

        return this.wsMessageService.subscribeChatRoom(roomId, chatUser.getEmail());
    }


    /**
     * 메세지 전송
     * @param roomId : 채팅방 Id
     * @param content : 채팅 메세지
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/chat-room/{roomId}")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleSendMessage(@DestinationVariable Long roomId, @Payload String content,
                                    SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("메세지 전송 : chat-room-{}, host : {} / message : {}", roomId, chatUser.getId(), content);

        return this.wsMessageService.sendMessage(chatUser, roomId, content);
    }

    /**
     * 채팅방 퇴장
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/chat-room/{roomId}/leave")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleUnsubscription(@DestinationVariable Long roomId,
                                       SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("채팅방 퇴장 : chat-room-{}, host : {}", roomId, chatUser.getId());

        return this.wsMessageService.unsubscribeChatRoom(chatUser.getEmail(), roomId);
    }

    /**
     * 채팅방 방장 승계
     * @param roomId : 채팅방 ID
     * @param newHost : 새로운 방장 이메일
     * @return
     */
    @MessageMapping("/chat-room/{roomId}/success")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleSuccession(@DestinationVariable Long roomId, @Payload String newHost) {
        log.info("채팅방 방장이 변경 : chat-room-{} new-host : {}", roomId, newHost);

        return this.wsMessageService.seccessHost(roomId, newHost);
    }

    /**
     * 헤더 엑세서에서 AuthUser getter
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    private WSChatUser getChatUser(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        if (attributes == null) {
            throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
        }

        WSChatUser chatUser = (WSChatUser) attributes.get("chatUser");

        if (chatUser == null) {
            throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
        }

        return chatUser;
    }
}
