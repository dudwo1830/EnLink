package net.datasa.EnLink.community.chat.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.chat.service.ChatService;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubMemberService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	private final ClubService clubService;
	private final ClubMemberService clubMemberService;
	private final ClubManageService clubManageService;
	
	/**
	 * 채팅방 입장
	 * 주소 예시: /community/chat/room/1
	 */
	@GetMapping("/room/{clubId}")
	public String goChatRoom(@PathVariable("clubId") Integer clubId,
							 @AuthenticationPrincipal MemberDetails member, Model model) {
		
		// 1. 서비스에서 해당 모임 멤버인지 권한 체크
		boolean isClubMember = clubMemberService.checkClubMember(clubId, member.getUsername());
		if (!isClubMember){
			return "redirect:/club/" + clubId;
		}
		model.addAttribute("loginMember", clubManageService.getMemberInfo(clubId, member.getMemberId()));
		
		// 2. 화면에서 필요한 정보 전달
		model.addAttribute("clubId", clubId);
		model.addAttribute("userId", member.getUsername());
		
		// 오른쪽 패널용 데이터
		model.addAttribute("club", clubService.getClubDetail(clubId));
		model.addAttribute("members", clubService.getActiveMembers(clubId));
		
		return "community/chat/chatRoom";
	}
}
