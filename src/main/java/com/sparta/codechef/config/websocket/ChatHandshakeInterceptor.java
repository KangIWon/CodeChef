package com.sparta.codechef.config.websocket;

import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.security.AuthUser;
import com.sparta.codechef.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        AuthUser authUser = this.getAuthUserFromRequest(request);
        if (authUser != null) {
            attributes.put("chatUser", ChatUser.fromAuthUser(authUser));  // WebSocket 세션에 사용자 정보 저장
            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    /**
     * ServerHttpRequest로 AuthUser Getter
     * @param request
     * @return
     */
    private AuthUser getAuthUserFromRequest(ServerHttpRequest request) {
        URI uri = request.getURI();
        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");
        return jwtUtil.validateToken(token);
    }
}
