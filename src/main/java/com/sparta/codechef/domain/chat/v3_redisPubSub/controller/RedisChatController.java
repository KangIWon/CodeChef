package com.sparta.codechef.domain.chat.v3_redisPubSub.controller;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redisPubSub.service.RedisMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedisChatController {

    private final RedisMessageService messageService;


    /**
     * 채팅방 입장(구독)
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/v3/chat-room/{roomId}/subscribe")
    public void handleSubscription(@DestinationVariable Long roomId,
                                   SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("subscribe : chat-room-{}, host : {}", roomId, chatUser.getId());
        this.messageService.getMessages(roomId, chatUser.getId());
        this.messageService.subscribeChatRoom(roomId, chatUser);
    }


    /**
     * 메세지 전송
     * @param roomId : 채팅방 Id
     * @param content : 채팅 메세지
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/v3/chat-room/{roomId}")
    public void handleSendMessage(@DestinationVariable Long roomId,
                                  @Payload String content,
                                  SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("send message : chat-room-{}, host : {} / message : {}", roomId, chatUser.getId(), content);

        this.messageService.sendMessage(chatUser, roomId, content);
    }

    /**
     * 채팅방 퇴장
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/v3/chat-room/{roomId}/leave")
    public void handleUnsubscription(@DestinationVariable Long roomId,
                                        @Payload UnsubscribeDto dto,
                                        SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("unsubscribe : chat-room-{}, host : {}", roomId, chatUser.getId());

        this.messageService.unsubscribeChatRoom(roomId, dto, chatUser);
    }

    /**
     * 헤더 엑세서에서 AuthUser getter
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    private ChatUser getChatUser(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        if (attributes == null) {
            throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
        }

        ChatUser chatUser = (ChatUser) attributes.get("chatUser");

        if (chatUser == null) {
            throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
        }

        return chatUser;
    }
}
