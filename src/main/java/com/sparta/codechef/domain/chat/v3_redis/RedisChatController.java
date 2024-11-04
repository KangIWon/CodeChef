package com.sparta.codechef.domain.chat.v3_redis;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v2.dto.UnsubscribeDTO;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import com.sparta.codechef.domain.chat.v2.service.WSMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedisChatController {

    private final RedisPublisher redisPublisher;
    private final WSMessageService wsMessageService;
    private final String TOPIC_KEY = "/topic/chat-room";


    /**
     * 이전 채팅 메세지 불러오기
     * @param roomId : 채팅방 ID
     * @return 채팅 메세지 리스트
     */
    @SubscribeMapping("/chat-room/{roomId}/subscribe")
    public void handleSubscription(@DestinationVariable Long roomId
    ) {
        log.info("채팅방 이전 메세지 불러오기 : chat-room-{}", roomId);
        List<WSMessage> messageList = this.wsMessageService.getMessages(roomId);

        this.redisPublisher.publish(new ChannelTopic(TOPIC_KEY + roomId), messageList);
    }


    /**
     * 채팅방 입장(구독)
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/chat-room/{roomId}/subscribe")
    public void handleSendWelcomeMessage(@DestinationVariable Long roomId,
                                              SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("채팅방 입장 : chat-room-{}, host : {}", roomId, chatUser.getId());

        WSMessage message = this.wsMessageService.subscribeChatRoom(roomId, chatUser);
        this.redisPublisher.publish(new ChannelTopic(TOPIC_KEY + roomId), message);
    }


    /**
     * 메세지 전송
     * @param roomId : 채팅방 Id
     * @param content : 채팅 메세지
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/chat-room/{roomId}")
    public void handleSendMessage(@DestinationVariable Long roomId, @Payload String content,
                                       SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("메세지 전송 : chat-room-{}, host : {} / message : {}", roomId, chatUser.getId(), content);

        WSMessage message = this.wsMessageService.sendMessage(chatUser, roomId, content);
        this.redisPublisher.publish(new ChannelTopic(TOPIC_KEY + roomId), message);
    }

    /**
     * 채팅방 퇴장
     * @param roomId : 채팅방 ID
     * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
     * @return
     */
    @MessageMapping("/chat-room/{roomId}/leave")
    public void handleUnsubscription(@DestinationVariable Long roomId,
                                                @Payload UnsubscribeDTO dto,
                                                SimpMessageHeaderAccessor headerAccessor
    ) {
        WSChatUser chatUser = this.getChatUser(headerAccessor);
        log.info("채팅방 퇴장 : chat-room-{}, host : {}", roomId, chatUser.getId());

        List<WSMessage> messageList = this.wsMessageService.unsubscribeChatRoom(roomId, chatUser, dto);
        this.redisPublisher.publish(new ChannelTopic(TOPIC_KEY + roomId), messageList);
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
