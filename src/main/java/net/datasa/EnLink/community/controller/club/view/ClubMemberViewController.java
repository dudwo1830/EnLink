package net.datasa.EnLink.community.controller.club.view;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/club/{clubId}/member") // {clubId}를 공통 경로로 사용
@RequiredArgsConstructor
public class ClubMemberViewController {
	
	private final ClubService clubService;
	private final ClubManageService clubManageService;
	
	/** 클럽 가입 신청 */
	@PostMapping("/apply")
	public String apply(@PathVariable("clubId") Integer clubId,
						@RequestParam("answer") String answer,
						RedirectAttributes rttr) {
		
		String loginId = "user10";
		clubService.applyToClub(clubId, loginId, answer);
		
		rttr.addFlashAttribute("message", "가입 신청이 완료되었습니다.");
		return "redirect:/club/" + clubId;
	}
	
	/** 클럽 가입 신청 취소 */
	@PostMapping("/cancel")
	public String cancelApply(@PathVariable("clubId") Integer clubId,
							  RedirectAttributes rttr) {
		
		clubService.cancelApplication(clubId, "user10");
		rttr.addFlashAttribute("message", "가입 신청이 취소되었습니다.");
		
		return "redirect:/club/" + clubId;
	}
	
	/** 회원 탈퇴 */
	@PostMapping("/leave")
	public String leave(@PathVariable Integer clubId,RedirectAttributes rttr) {
		String loginId = "user10";
		
		clubService.leaveClub(clubId, loginId);
		// ⭐ REST 방식의 ResponseEntity 대신 리다이렉트와 메시지 전달 사용
		rttr.addFlashAttribute("message", "탈퇴가 완료되었습니다.");
		clubManageService.leaveHistory(clubId, loginId, loginId, "LEAVE", "사용자가 직접 모임을 탈퇴하였습니다.");
		return "redirect:/club/" + clubId;
	}
}
