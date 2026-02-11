package net.datasa.EnLink.community.chat.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.chat.dto.ChatMessageDTO;
import net.datasa.EnLink.community.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {
	
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	
	/**
	 * 클라이언트가 메시지를 보낼 때 호출됨
	 * 주소: /pub/chat/message
	 */
	@MessageMapping("/chat/message")
	public void handleMessage(ChatMessageDTO message, Principal principal) {
		// 컨트롤러는 principal만 넘겨주고,
		// senderId 세팅 및 저장은 서비스에 위임
		ChatMessageDTO savedMessage = chatService.saveMessage(message, principal);
		// 저장된 데이터를 전송
		messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessage.getClubId(), savedMessage);
	}
}
