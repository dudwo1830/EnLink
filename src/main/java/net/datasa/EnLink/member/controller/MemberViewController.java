package net.datasa.EnLink.member.controller;

import jakarta.servlet.http.HttpSession;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.service.ClubMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

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
	/**
	 * 회원 가입 페이지
	 * 
	 * @return
	 */
	@GetMapping("")
	public String signup() {
		return TEMPLATE_PATH + "signup";
	}
	
	
	@GetMapping("/mypage/clubs")
	public String myClubs(@RequestParam(value = "type", defaultValue = "owned") String type,
						  Model model, HttpSession session) {
		
		String loginId = "user10"; // 임시
		Map<String, List<ClubDTO>> allClubs = clubMemberService.getMyClubs(loginId);
		
		// 선택한 type에 맞는 데이터와 제목만 넘김
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
}

