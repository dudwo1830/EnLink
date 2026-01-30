package net.datasa.EnLink.community.controller.club.view;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubMemberDTO;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/club")
@Controller
public class ClubViewController {
	
	private final ClubService clubService;
	private final ClubManageService clubManageService;
	
	/**
	 * 모임생성 폼 이동
	 * // TODO: Spring Security 적용 시 수정
	 */
	@GetMapping("/create")
	public String createForm(Model model, @SessionAttribute(name = "loginMember", required = false) Member loginMember){
		model.addAttribute("clubDTO", new ClubDTO());
		
		if (loginMember == null) {
			return "redirect:/login";
		}
		
		return "club/createClubForm";
	}
	
	/**
	 * 모임 생성 (처리 후 리스트로 이동 유도)
	 */
	@PostMapping("/create")
	public String create(@ModelAttribute("clubDTO") ClubDTO clubDTO) {
		String loginId = "bgh_leader";
		clubService.createClub(clubDTO, loginId);
		
		return "redirect:/club/clubList";
	}
	
	/**
	 * 모임 목록 조회
	 */
	@GetMapping("/list")
	public String list(Model model) {
		List<ClubDTO> clubs = clubService.getClubList();
		model.addAttribute("clubs", clubs);
		return "club/clubList";
	}
	
	/**
	 * 클럽 상세조회
	 */
	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Integer id, Model model) {
		
		String loginId = "user10";
		
		ClubDTO club = clubService.getClubDetail(id);
		List<ClubMemberDTO> members = clubService.getActiveMembers(loginId);
		
		// 1. 여기서 이미 ACTIVE 여부를 체크해서 가져옵니다.
		ClubMemberDTO loginMember = clubManageService.getMemberInfo(id, loginId);
		
		model.addAttribute("club", club);
		model.addAttribute("members", members);
		model.addAttribute("loginMember", loginMember);
		
		// 2. ⭐ applyStatus도 loginMember 상태에 따라 결정합니다.
		// loginMember가 null이면 신청 안 한 상태(null), 아니면 현재 상태(ACTIVE)
		String applyStatus = (loginMember != null) ? loginMember.getStatus() : null;
		model.addAttribute("applyStatus", applyStatus);
		
		System.out.println("Status: " + applyStatus);
		
		return "club/clubDetail";
	}
}
