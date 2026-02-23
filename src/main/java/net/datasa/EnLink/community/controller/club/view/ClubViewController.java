package net.datasa.EnLink.community.controller.club.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.request.ClubCreateRequest;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubService;
import net.datasa.EnLink.topic.service.TopicService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/club")
@Controller
public class ClubViewController {
	
	private final ClubService clubService;
	private final ClubManageService clubManageService;
	private final ClubMemberRepository clubMemberRepository;
	private final TopicService topicService;
	
	/** 모임 생성 폼 이동 */
	@GetMapping("/create")
	public String createForm(Model model, @AuthenticationPrincipal MemberDetails loginUser) {
		if (loginUser == null) return "redirect:/auth/login";
		
		
		long ownerCount = clubMemberRepository.countOwnerQuota(loginUser.getUsername());
		
		if (ownerCount >= 5) {
			throw new BusinessException(ErrorCode.OWNER_LIMIT_EXCEEDED);
		}
		
		model.addAttribute("topics", topicService.getListAll());
		
		ClubCreateRequest clubCreateRequest = new ClubCreateRequest();
		
		model.addAttribute("clubCreateRequest", clubCreateRequest);
		
		return "club/createClubForm";
	}
	
	/** 모임 목록 조회 */
	@GetMapping("/list")
	public String list(Model model,
						@AuthenticationPrincipal MemberDetails loginUser,
						@RequestParam(name="topicId", required = false, defaultValue = "") Integer topicId) {
						String loginMemberId = (loginUser != null) ? loginUser.getUsername() : null;
		
		model.addAttribute("clubs", clubService.getListByTopicId(topicId));
		model.addAttribute("loginMemberId", loginMemberId);
		return "club/clubList";
	}
	
	/** 클럽 상세조회 */
	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Integer id,
						 @AuthenticationPrincipal MemberDetails loginUser,
						 Model model) {
		
		String loginId = (loginUser != null) ? loginUser.getMemberId() : null;
		
		model.addAttribute("isLogin", loginUser != null);
		model.addAttribute("club", clubService.getClubDetail(id));
		model.addAttribute("members", clubService.getActiveMembers(id));
		model.addAttribute("loginMember", clubManageService.getMemberInfo(id, loginId));
		model.addAttribute("applyStatus", clubManageService.getApplyStatus(id, loginId));
		
		long myParticipantCount = 0;
		if (loginId != null) {
			myParticipantCount = clubMemberRepository.countParticipantQuota(loginId);
		}
		
		model.addAttribute("canJoin", myParticipantCount < 5);
		
		return "club/clubDetail";
	}
}
