//package com.sparta.codechef.domain.chat.v2.config;
//
//import com.sparta.codechef.config.websocket.ChatHandshakeInterceptor;
//import com.sparta.codechef.security.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@RequiredArgsConstructor
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final JwtUtil jwtUtil;
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic", "/queue"); // 구독 경로
//        registry.setApplicationDestinationPrefixes("/app"); // 송신 경로
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws-chat")  // STOMP 엔드포인트
//                .addInterceptors(new ChatHandshakeInterceptor(jwtUtil))
//                .setAllowedOriginPatterns("*");
//
//        registry.addEndpoint("/ws-alarm") // 알림용 추가 엔드포인트
//                .setAllowedOriginPatterns("*");
//                //.withSockJS(); // 안돌아가면 이거 빼고 테스트
//    }
//}