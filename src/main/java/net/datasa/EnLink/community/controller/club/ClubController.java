package net.datasa.EnLink.community.controller.club;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
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
	public String createClubForm(Model model){
		model.addAttribute("clubDTO", new ClubDTO());
		return "club/createClubForm";
	}
	
	@PostMapping("/create")
	public String createClub(@ModelAttribute("clubDTO") ClubDTO clubDTO) {
		// 1. 데이터 확인용 로그 (개발 단계에서 아주 중요!)
		System.out.println("전송된 모임 데이터: " + clubDTO);
		
		// 2. 현재 로그인한 사용자 ID 가져오기
		// (아직 로그인 기능 전이라면 임시로 배경호 님의 ID를 사용하세요)
		String loginId = "bgh_leader";
		
		// 3. 서비스 호출하여 저장 로직 실행
		// (DTO를 서비스로 넘겨서 처리합니다)
		clubService.createClub(clubDTO, loginId);
		
		// 4. 완료 후 목록 화면으로 이동 (새로고침 중복 방지를 위해 redirect 사용)
		return "redirect:/club/list";
	}
	
	@GetMapping("/list")
	public String list(Model model) {
		List<ClubEntity> clubs = clubService.getAllClubs();
		model.addAttribute("clubs", clubs);
		return "club/clubList"; // templates/club/clubList.html
	}
	
	// 모임 상세 정보 조회
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model) {
		ClubEntity club = clubService.getClubById(id);
		model.addAttribute("club", club);
		return "club/clubDetail"; // templates/club/clubDetail.html
	}
	
	// 수정 폼 띄우기
	@GetMapping("/modify/{id}")
	public String modifyForm(@PathVariable("id") Integer id, Model model) {
		ClubEntity club = clubService.getClubById(id);
		model.addAttribute("clubDTO", club); // 기존 데이터를 DTO나 Entity에 담아 전달
		return "club/modifyClubForm";
	}
	
	// 수정 처리
	@PostMapping("/modify/{id}")
	public String modify(@PathVariable("id") Integer id, @ModelAttribute("clubDTO") ClubDTO clubDTO) {
		clubService.updateClub(id, clubDTO);
		return "redirect:/club/detail/" + id;
	}
}
