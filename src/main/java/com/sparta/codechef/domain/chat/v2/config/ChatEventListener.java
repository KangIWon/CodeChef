package com.sparta.codechef.domain.chat.v2.config;

import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.service.WSChatService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final WSChatService WSChatService;

    // 웹 소켓 연결 요청 처리
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication();

        if (authUser!= null) {
            WSChatUser chatUser = WSChatUser.fromAuthUser(authUser);
            this.WSChatService.connectChatUser(chatUser);
        }

        log.info("WebSocket connected, sessionId : {}", sessionId);
    }


    // 웹 소켓 구독 요청 처리
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket subscribe, sessionId : {}", sessionId);
    }

    // 웹 소켓 구독 취소 요청 처리
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket unsubscribe, sessionId : {}", sessionId);
    }

    // 웹 소켓 연결 해제 요청 처리
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication();

        if (authUser!= null) {
            WSChatUser chatUser = WSChatUser.fromAuthUser(authUser);
            this.WSChatService.disconnectChatUser(chatUser);
        }

        log.info("WebSocket disconnected, sessionId : {}", sessionId);
    }
}
