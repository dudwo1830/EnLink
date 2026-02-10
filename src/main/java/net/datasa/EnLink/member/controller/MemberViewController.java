package net.datasa.EnLink.member.controller;

import jakarta.servlet.http.HttpSession;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.service.ClubMemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.dto.response.MemberUpdateResponse;
import net.datasa.EnLink.member.service.MemberService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("members")
@RequiredArgsConstructor
public class MemberViewController {
	private final String TEMPLATE_PATH = "member/";
	private final ClubMemberService clubMemberService;
	private final MemberService memberService;

	/**
	 * 회원 가입 페이지
	 * 
	 * @return
	 */
	@GetMapping("signup")
	public String signup() {
		return TEMPLATE_PATH + "signup";
	}
	
	
	@GetMapping("/mypage/clubs")
	public String myClubs(@RequestParam(value = "type", defaultValue = "owned") String type,
						  @AuthenticationPrincipal MemberDetails loginUser,
						  Model model, HttpSession session) {
		
		if (loginUser == null) {
			return "redirect:/member/login";
		}
		String loginId = loginUser.getMemberId();
		
		Map<String, List<ClubDTO>> allClubs = clubMemberService.getMyClubs(loginId);
		
		switch (type) {
			case "active" -> {
				model.addAttribute("clubs", allClubs.get("activeClubs"));
				model.addAttribute("title", "🤝 참여 중인 모임");
			}
			case "pending" -> {
				model.addAttribute("clubs", allClubs.get("pendingClubs"));
				model.addAttribute("title", "⏳ 가입 신청 현황");
			}
			default -> { // owned
				model.addAttribute("clubs", allClubs.get("ownedClubs"));
				model.addAttribute("title", "👑 내가 만든 모임");
			}
		}
		
		model.addAttribute("currentType", type); // 현재 탭 표시용
		return "member/myClubList";
	}

	@GetMapping("me/edit")
	public String edit(@AuthenticationPrincipal MemberDetails member, Model model) {
		MemberUpdateResponse response = memberService.edit(member.getMemberId());
		model.addAttribute("member", response);
		return TEMPLATE_PATH + "edit";
	}

	/**
	 * 회원 정보 수정 페이지
	 * 
	 * @param memberId
	 * @param model
	 * @return
	 */
	@GetMapping("me")
	public String read(@AuthenticationPrincipal MemberDetails member, Model model) {
		MemberDetailResponse response = memberService.read(member.getMemberId());
		model.addAttribute("member", response);
		return TEMPLATE_PATH + "detail";
	}

	/**
	 * 관심사 관리 페이지
	 * 
	 * @param memberId
	 * @return
	 */
	@GetMapping("me/interest")
	public String interest(@AuthenticationPrincipal MemberDetails user, Model model) {
		return TEMPLATE_PATH + "interest";
	}

}

