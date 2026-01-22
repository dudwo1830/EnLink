package net.datasa.EnLink.community.controller.club;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/club/{clubId}/manage")
@Controller
@RequiredArgsConstructor
public class ClubManageController {
	
	private final ClubService clubService;
	
	
	@GetMapping("/requests")
	public String manageRequests(@PathVariable("clubId") Integer clubId, Model model) {
		// 1. 서비스에서 신청자 리스트를 가져와야 합니다.
		List<ClubJoinRequestDTO> requests = clubService.getPendingRequests(clubId);
		
		// 2. HTML에서 사용할 'joinRequests'라는 이름과 일치해야 합니다.
		model.addAttribute("joinRequests", requests);
		model.addAttribute("clubId", clubId);
		
		return "club/manage/requests"; // 해당 HTML 파일명
	}
	
	// 수정 폼
	@GetMapping("/edit")
	public String edit(@PathVariable Integer clubId, Model model) {
		
		ClubEntity club = clubService.getClubById(clubId);
		
		ClubDTO clubDTO = ClubDTO.builder()
				.clubId(club.getClubId())
				.name(club.getName())
				.topicId(club.getTopicId())
				.cityId(club.getCityId())
				.description(club.getDescription())
				.maxMember(club.getMaxMember())
				.joinQuestion(club.getJoinQuestion())
				.imageUrl(club.getImageUrl())
				.build();
		
		model.addAttribute("clubDTO", clubDTO);
		return "club/manage/clubEdit";
	}
	
	// 수정 처리
	@PostMapping("/edit")
	public String editClub(
			@PathVariable Integer clubId,
			@ModelAttribute ClubDTO clubDTO) {
		
		clubService.updateClub(clubId, clubDTO);
		return "redirect:/club/" + clubId;
	}
	
	// 수정예정
	@GetMapping("/members")
	public String members(@PathVariable("clubId") Integer clubId, Model model) {
	
		model.addAttribute("clubId", clubId);
		return "club/manage/members";
	}
	
	@GetMapping("/delete")
	public String deleteForm(@PathVariable("clubId") Integer clubId, Model model) {
		// 1. 서비스에서 모임 상세 정보(DTO)를 가져옵니다.
		ClubDTO club = clubService.getClubDetail(clubId);
		
		// 2. HTML에서 사용할 수 있도록 "club"이라는 이름으로 객체를 통째로 넘깁니다.
		model.addAttribute("club", club);
		
		return "club/manage/clubDelete";
	}
	
	@PostMapping("/delete")
	public String delete(@PathVariable("clubId") Integer clubId) {
		clubService.deleteClub(clubId);
		return "redirect:/club/list"; // 삭제 후에는 보통 목록으로 보냅니다.
	}
	
	@PostMapping("/restore")
	public String restore(@PathVariable("clubId") Integer clubId) {
		clubService.restoreClub(clubId);
		// 복구 후에는 다시 상세 페이지나 목록으로 보냅니다.
		return "redirect:/club/list";
	}
	
	/**
	 * 가입 신청 승인 처리
	 * @param clubId 모임 ID
	 * @param memberId 승인할 유저 ID
	 */
	@PostMapping("/approve")
	public String approve(@PathVariable("clubId") Integer clubId, @RequestParam("memberId") String memberId) {
		// 서비스 호출하여 상태를 'ACTIVE'로 변경
		clubService.approveMember(clubId, memberId);
		
		// 처리 후 다시 신청 현황 목록으로 리다이렉트
		return "redirect:/club/" + clubId + "/manage/requests";
	}
	
	/**
	 * 가입 신청 거절 처리
	 * @param clubId 모임 ID
	 * @param memberId 거절할 유저 ID
	 */
	@PostMapping("/reject")
	public String reject(@PathVariable("clubId") Integer clubId, @RequestParam("memberId") String memberId) {
		// 서비스 호출하여 신청 데이터 및 답변 삭제
		clubService.rejectMember(clubId, memberId);
		
		// 처리 후 다시 신청 현황 목록으로 리다이렉트
		return "redirect:/club/" + clubId + "/manage/requests";
	}
	
}
