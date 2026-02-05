package net.datasa.EnLink.community.controller.club.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/club")
@Controller
public class ClubViewController {
	
	private final ClubService clubService;
	private final ClubManageService clubManageService;
	private final ClubMemberRepository clubMemberRepository;
	
	/** 모임 생성 폼 이동 */
	@GetMapping("/create")
	public String createForm(Model model, @AuthenticationPrincipal MemberDetails loginUser) {
		if (loginUser == null) return "redirect:/auth/login";
		
		
		long totalActiveCount = clubMemberRepository.countByMember_MemberIdAndStatus(loginUser.getMemberId(), "ACTIVE");
		if (totalActiveCount >= 5) {
			log.info("가입 제한 초과 확인됨! 에러 발생시킵니다.");
			throw new BusinessException(ErrorCode.CLUB_JOIN_LIMIT_EXCEEDED);
		}
		
		model.addAttribute("clubDTO", new ClubDTO());
		return "club/createClubForm";
	}
	
	/** 모임 목록 조회 */
	@GetMapping("/list")
	public String list(Model model, @AuthenticationPrincipal MemberDetails loginUser) {
		String loginId = (loginUser != null) ? loginUser.getUsername() : null;
		model.addAttribute("clubs", clubService.getClubList());
		model.addAttribute("loginId", loginId);
		return "club/clubList";
	}
	
	/** 클럽 상세조회 */
	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Integer id, @AuthenticationPrincipal MemberDetails loginUser, Model model) {
		String loginId = (loginUser != null) ? loginUser.getMemberId() : null;
		
		model.addAttribute("club", clubService.getClubDetail(id));
		model.addAttribute("members", clubService.getActiveMembers(id));
		model.addAttribute("loginMember", clubManageService.getMemberInfo(id, loginId));
		model.addAttribute("applyStatus", clubManageService.getApplyStatus(id, loginId));
		
		long totalActiveCount = (loginId != null) ? clubMemberRepository.countByMember_MemberIdAndStatus(loginId, "ACTIVE") : 0;
		model.addAttribute("totalActiveCount", totalActiveCount);
		
		return "club/clubDetail";
	}
}
