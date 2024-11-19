package com.sparta.codechef.config.websocket;

import com.sparta.codechef.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Value("${websocket.host}")
    private String host;
    @Value("${websocket.port}")
    private int port;
    @Value("${spring.rabbitmq.client.username}")
    private String clientUsername;
    @Value("${spring.rabbitmq.client.password}")
    private String clientPassword;
    @Value("${spring.rabbitmq.system.username}")
    private String systemUsername;
    @Value("${spring.rabbitmq.system.password}")
    private String systemPassword;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayPort(port)
                .setRelayHost(host)
                .setSystemLogin(systemUsername)
                .setSystemPasscode(systemPassword)
                .setClientLogin(clientUsername)
                .setClientPasscode(clientPassword)
                .setVirtualHost("/")
                .setAutoStartup(true);
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app"); // 송신 경로
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")  // STOMP 엔드포인트
                .addInterceptors(new ChatHandshakeInterceptor(jwtUtil))
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-alarm")
                .setAllowedOriginPatterns("*");
    }
}