package net.datasa.EnLink.community.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker	// WebSocket 메시지 핸들링 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 클라이언트가 처음 WebSocket에 연결할 주소
		// setAllowedOriginPatterns("*")는 모든 도메인에서의 접속을 허용합니다.
		registry.addEndpoint("/ws-chat")
				.setAllowedOriginPatterns("*")
				.withSockJS(); // 브라우저 호환성을 위한 SockJS설정
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지를 받을 때 (구독): 서버 -> 클라이언트
		// 클라이언트는 /sub/chat/룸ID 주소를 구독하게 됩니다.
		registry.enableSimpleBroker("/sub");
		
		// 메시지를 보낼 때 (발행): 클라이언트 -> 서버
		// 클라이언트는 /pub/chat/message 주소로 메시지를 보냅니다.
		registry.setApplicationDestinationPrefixes("/pub");
	}
}
