package com.sparta.codechef.domain.chat.v2.controller;

import com.sparta.codechef.domain.chat.v1.dto.request.MessageRequest;
import com.sparta.codechef.domain.chat.v2.dto.NewHostRequest;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import com.sparta.codechef.domain.chat.v2.service.WSChatService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WSController {

    private final WSChatService WSChatService;

    /**
     * 채팅방 개설
     * @param roomId : 채팅방 ID
     * @return
     */
    @MessageMapping("/chats/rooms/{roomId}/create")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleCreateChatRoom(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long roomId) {

        WSMessage wsMessage = WSMessage.builder()
                                .topic(roomId)
                                .sender(authUser.getUserId())
                                .content("채팅방이 개설되었습니다.")
                                .build();

        return wsMessage;
    }



    /**
     * 채팅방 입장 (구독 요청)
     * @return 입장 안내 메세지
     */
    @SubscribeMapping("/chats/rooms/{roomId}/enter")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleSubscription(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long roomId) {
        this.WSChatService.subscribeChatRoom(roomId, authUser.getUserId());
        String content = new StringBuffer(authUser.getEmail()).append("님이 채팅방에 입장하셨습니다.").toString();
        WSMessage wsMessage = WSMessage.builder()
                .topic(roomId)
                .sender(authUser.getUserId())
                .content(content)
                .build();

        // 메세지 저장
        return wsMessage;
    }

    /**
     * 메세지 전송
     * @param authUser :
     * @param roomId
     * @param request
     * @return
     */
    @MessageMapping("/chats/rooms/{roomId}")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleSendMessage(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long roomId, MessageRequest request) {
        WSMessage wsMessage = WSMessage.builder()
                .topic(roomId)
                .sender(authUser.getUserId())
                .content(request.getMessage())
                .build();
        // 메세지 저장
        return wsMessage;
    }

    // 채팅방 퇴장
    @MessageMapping("/chats/rooms/{roomId}/leave")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleUnsubscription(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long roomId) {
        String content = new StringBuffer(authUser.getEmail()).append("님이 채팅방에서 퇴장하셨습니다.").toString();
        WSMessage wsMessage = WSMessage.builder()
                .topic(roomId)
                .sender(authUser.getUserId())
                .content(content)
                .build();

        // 메세지 저장
        return wsMessage;
    }

    // 방장 승계
    @MessageMapping("/chats/rooms/{roomId}/success")
    @SendTo("/topic/chat-room/{roomId}")
    public WSMessage handleSuccession(@PathVariable Long roomId, @Payload NewHostRequest request) {
        String content = new StringBuffer(request.getHostEmail()).append("님이 새로운 방장이 되셨습니다.").toString();
        WSMessage wsMessage = WSMessage.builder()
                .topic(roomId)
                .content(content)
                .build();

        // 메세지 저장
        return wsMessage;
    }
}
