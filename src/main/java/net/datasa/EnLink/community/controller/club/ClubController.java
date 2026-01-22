package net.datasa.EnLink.community.controller.club;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/club")
@Controller
public class ClubController {
	
	final ClubService clubService;
	
	@GetMapping("/create")
	public String createForm(Model model){
		model.addAttribute("clubDTO", new ClubDTO());
		return "club/createClubForm";
	}
	
	@PostMapping("/create")
	public String create(@ModelAttribute("clubDTO") ClubDTO clubDTO) {
		
		// 1. 시큐리티 대신 임시로 DB에 존재하는 아이디를 넣습니다.
		// (영재님이 만든 members 테이블에 "bgh_leader"라는 ID가 있다고 가정)
		String loginId = "bgh_leader";
		
		System.out.println("임시 로그인 유저: " + loginId + "가 모임을 생성합니다.");
		
		// 2. 서비스 호출 (기존과 동일)
		clubService.createClub(clubDTO, loginId);
		
		return "redirect:/club/list";
	}
	
	@GetMapping("/list")
	public String list(Model model) {
		List<ClubDTO> clubs = clubService.getClubList();
		model.addAttribute("clubs", clubs);
		return "club/clubList"; // templates/club/clubList.html
	}
	
	/**
	 * 클럽 상세조회
	 * */
	
	@GetMapping("{id}")
	public String detail(@PathVariable("id") Integer id, Model model) {
		ClubDTO club = clubService.getClubDetail(id);
		String loginId = "bgh_guest"; // 현재 로그인한 유저 ID (가정)
		
		// 이 유저의 해당 모임 신청 상태를 가져옵니다 (PENDING, ACCEPTED 등)
		String applyStatus = clubService.getApplicationStatus(id, loginId);
		
		model.addAttribute("club", club);
		model.addAttribute("applyStatus", applyStatus);
		return "club/clubDetail";
	}
	
	/**
	 * 클럽가입신청
	 * */
	
	@PostMapping("/{clubId}/apply")
	public String apply(@PathVariable("clubId") Integer clubId, @RequestParam("answer") String answer) {
		String loginId = "bgh_guest"; // 테스트용
		clubService.applyToClub(clubId, loginId, answer);
		return "redirect:/club/" + clubId;
	}
	
	/**
	 * 클럽가입신청 취소
	 * */
	
	@PostMapping("/{clubId}/apply/cancel")
	public String cancelApply(@PathVariable("clubId") Integer clubId) {
		String loginId = "bgh_guest";
		clubService.cancelApplication(clubId, loginId); // 이 서비스 메서드도 만들어야 합니다.
		return "redirect:/club/" + clubId;
	}
	
	/**
	 * 회원탈퇴(회원 스스로)
	 * */
	
	
	@PostMapping("/{clubId}/leave")
	public String leave(@PathVariable("clubId") Integer clubId) {
		String loginId = "bgh_guest"; // 현재는 테스트용 아이디
		
		clubService.leaveClub(clubId, loginId);
		
		// 탈퇴 후에는 다시 상세 페이지로 이동 (그러면 '가입 신청하기' 버튼이 뜨겠죠?)
		return "redirect:/club/" + clubId;
	}
	
}
