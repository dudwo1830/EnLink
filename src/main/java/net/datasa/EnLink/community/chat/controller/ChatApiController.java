package net.datasa.EnLink.community.chat.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.chat.dto.ChatMessageDTO;
import net.datasa.EnLink.community.chat.entity.ChatMessageEntity;
import net.datasa.EnLink.community.chat.service.ChatService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {
	
	private final ChatService chatService;
	
	/**
	 * 채팅 내역 조회 (무한 스크롤용)
	 * 주소: GET /api/chat/history/1
	 */
	@GetMapping("/history/{clubId}")
	public ResponseEntity<Slice<ChatMessageDTO>> getHistory(
			@PathVariable("clubId") Integer clubId,
			@PageableDefault(size = 20) Pageable pageable) {
		
		// 서비스 호출 (이미 리포지토리에서 정렬을 하므로 별도의 sort 파라미터가 없어도 최신순으로 가져옵니다)
		Slice<ChatMessageDTO> history = chatService.getChatHistory(clubId, pageable);
		return ResponseEntity.ok(history);
	}
}
