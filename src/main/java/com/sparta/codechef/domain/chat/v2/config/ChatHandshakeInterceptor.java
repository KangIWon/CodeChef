package com.sparta.codechef.domain.chat.v2.config;

import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.security.AuthUser;
import com.sparta.codechef.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = this.extractTokenFromRequest(request);

        if (token != null && jwtUtil.validateToken(token)) {
            AuthUser authUser = jwtUtil.getAuthUserFromToken(token);

            attributes.put("chatUser", WSChatUser.fromAuthUser(authUser));  // WebSocket 세션에 사용자 정보 저장
            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        String query = request.getURI().getQuery();

        if (query != null && query.contains("token=")) {
            return query.split("token=")[1];
        }
        return null;
    }
}
