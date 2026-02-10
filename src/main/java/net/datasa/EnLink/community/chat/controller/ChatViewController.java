package net.datasa.EnLink.community.chat.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.chat.service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/community/chat")
@RequiredArgsConstructor
public class ChatViewController {
	
	private final ChatService chatService;
	
	/**
	 * 채팅방 입장
	 * 주소 예시: /community/chat/room/1
	 */
	@GetMapping("/room/{clubId}")
	public String goChatRoom(@PathVariable("clubId") Integer clubId,
							 @AuthenticationPrincipal UserDetails user, Model model) {
		
		// 1. 서비스에서 해당 모임 멤버인지 권한 체크
		chatService.checkChatAccess(clubId, user.getUsername());
		
		// 2. 화면에서 필요한 정보 전달
		model.addAttribute("clubId", clubId);
		model.addAttribute("userId", user.getUsername());
		
		return "community/chat/chatRoom";
	}
}
