package com.sparta.codechef.domain.chat.v2.config;

import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.repository.WSWSChatUserRepository;
import com.sparta.codechef.domain.chat.v2.service.WSChatService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "WSChatHandler")
@RequiredArgsConstructor
public class WSChatHandler extends TextWebSocketHandler {

    private final WSChatService wsChatService;

    // WS 연결 후,
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("session connected : {}", session.getId());
        this.getChatUser().ifPresent(this.wsChatService::connectChatUser);
    }

    // WS 연결 해체 후,
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("session disconnected : {}", session.getId());
        this.getChatUser().ifPresent(this.wsChatService::disconnectChatUser);
    }

    private Optional<WSChatUser> getChatUser()  {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication();

        if (authUser!= null) {
            return Optional.of(WSChatUser.fromAuthUser(authUser));
        }

        return Optional.empty();
    }
}
