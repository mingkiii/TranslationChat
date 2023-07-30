package com.example.translationchat.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 서버로 메시지를 보낼 때 prefix 설정 (클라이언트가 "/app"으로 시작하는 경로로 메시지를 보냄)
        config.setApplicationDestinationPrefixes("/app");

        // 서버가 클라이언트에게 메시지를 보낼 때 prefix 설정 (서버에서 "/topic"으로 시작하는 경로로 메시지를 보냄)
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 수립하는데 사용할 엔드포인트 설정
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(stompHandler);
//    }
}
