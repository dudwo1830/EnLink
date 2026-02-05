package net.datasa.EnLink.community.controller.club.view;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.service.ClubMemberHistoryService;
import net.datasa.EnLink.community.service.ClubMemberService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	private final ClubMemberService clubMemberService;
	private final ClubMemberHistoryService clubMemberHistoryService;
	
	/** 클럽 가입 신청 */
	@PostMapping("/apply")
	public String apply(@PathVariable("clubId") Integer clubId,
						@RequestParam("answer") String answer,
						@AuthenticationPrincipal MemberDetails userDetails,
						RedirectAttributes rttr) {
		
		if (userDetails == null) return "redirect:/auth/login";
		
		
		String loginId = userDetails.getUsername();
		clubService.applyToClub(clubId, loginId, answer);
		
		rttr.addFlashAttribute("message", "가입 신청이 완료되었습니다.");
		return "redirect:/club/" + clubId;
	}
	
	/** 클럽 가입 신청 취소 */
	@PostMapping("/cancel")
	public String cancelApply(@PathVariable("clubId") Integer clubId,
							  @AuthenticationPrincipal MemberDetails userDetails,
							  RedirectAttributes rttr) {
		
		
		if (userDetails == null) return "redirect:/auth/login";
		
		String loginId = userDetails.getUsername();
		clubService.cancelApplication(clubId, loginId);
		
		rttr.addFlashAttribute("message", "가입 신청이 취소되었습니다.");
		return "redirect:/club/" + clubId;
	}
}
