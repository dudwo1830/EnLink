	package net.datasa.EnLink.community.controller.club.view;
	
	import lombok.RequiredArgsConstructor;
	import net.datasa.EnLink.community.dto.ClubDTO;
	import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
	import net.datasa.EnLink.community.dto.ClubMemberDTO;
	import net.datasa.EnLink.community.service.ClubManageService;
	import net.datasa.EnLink.community.service.ClubService;
	import org.springframework.data.domain.Page;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.*;
	import org.springframework.web.servlet.mvc.support.RedirectAttributes;
	
	import java.util.List;
	
	@RequestMapping("/club/{clubId}/manage")
	@Controller
	@RequiredArgsConstructor
	public class ClubManageViewController {
		
		private final ClubService clubService;
		private final ClubManageService clubManageService;
		
		/** 가입 신청 현황 페이지 이동 */
		@GetMapping("/requests")
		public String manageRequests(
				@PathVariable("clubId") Integer clubId,
				@RequestParam(value = "page", defaultValue = "0") int page, // 페이지 번호 추가
				Model model,
				RedirectAttributes rttr) {
			
			String loginId = "user15"; // 실제로는 세션이나 시큐리티에서 가져온 로그인 ID
			
			try {
				// 1. 운영진 이상 접근 가능 여부 체크 (에러 발생 시 catch 블록으로 이동)
				clubManageService.checkAuthority(clubId, loginId, "MANAGER_UP");
				
				ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, loginId);
				model.addAttribute("loginMember", loginMember);
				
				Page<ClubJoinRequestDTO> requestPage = clubManageService.getPendingRequestsPaging(clubId, page);
				model.addAttribute("joinRequests", requestPage.getContent());
				model.addAttribute("paging", requestPage);
				model.addAttribute("clubId", clubId);
				
				return "club/manage/requests";
				
			} catch (Exception e) {
				// 2. 권한이 없을 경우 에러 메시지와 함께 튕겨내기
				rttr.addFlashAttribute("message", e.getMessage());
				// 해당 모임의 상세 페이지나 메인 페이지로 보냅니다.
				return "redirect:/club/" + clubId;
			}
		}
		
		/** 모임 정보 수정 페이지 이동 */
		@GetMapping("/edit")
		public String edit(@PathVariable Integer clubId, Model model, RedirectAttributes rttr) {
			String loginId = "user15"; // 테스트용 ID
			
			try {
				// ⭐ 모임장(LEADER)만 접근 가능하도록 체크
				clubManageService.checkAuthority(clubId, loginId, "MANAGER_UP");
				
				ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, loginId);
				model.addAttribute("loginMember", loginMember);
				
				ClubDTO clubDTO = clubManageService.getClubForEdit(clubId);
				model.addAttribute("clubDTO", clubDTO);
				return "club/manage/clubEdit";
				
			} catch (Exception e) {
				rttr.addFlashAttribute("message", e.getMessage());
				return "redirect:/club/" + clubId;
			}
		}
		
		/** 모임 삭제 페이지 이동 */
		@GetMapping("/delete")
		public String deleteForm(@PathVariable("clubId") Integer clubId, Model model, RedirectAttributes rttr) {
			String loginId = "user15"; // 테스트용 ID
			
			try {
				// ⭐ 모임장(LEADER)만 접근 가능하도록 체크
				clubManageService.checkAuthority(clubId, loginId, "LEADER_ONLY");
				
				ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, loginId);
				model.addAttribute("loginMember", loginMember);
				
				ClubDTO club = clubService.getClubDetail(clubId);
				model.addAttribute("club", club);
				return "club/manage/clubDelete";
				
			} catch (Exception e) {
				rttr.addFlashAttribute("message", e.getMessage());
				return "redirect:/club/" + clubId;
			}
		}
		
		/** 멤버 관리 페이지 이동 */
		@GetMapping("/members")
		public String manageMembers(@PathVariable("clubId") Integer clubId, Model model, RedirectAttributes rttr) {
			String loginId = "user15"; // 테스트용
			
			try {
				// 1. 운영진 이상(MANAGER_UP) 권한 체크
				clubManageService.checkAuthority(clubId, loginId, "MANAGER_UP");
				
				
				
				List<ClubMemberDTO> members = clubManageService.getActiveMembers(clubId);
				ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, loginId);
				
				model.addAttribute("members", members);
				model.addAttribute("loginMember", loginMember);
				model.addAttribute("clubId", clubId);
				return "club/manage/members";
				
			} catch (Exception e) {
				// 2. 권한 부족 시 상세 페이지로 리다이렉트
				rttr.addFlashAttribute("message", e.getMessage());
				return "redirect:/club/" + clubId;
			}
		}
		
		/** 모임 수정 */
		@PostMapping("/edit")
		public String editClub(@PathVariable Integer clubId, @ModelAttribute ClubDTO clubDTO) {
			clubManageService.updateClub(clubId, clubDTO);
			return "redirect:/club/" + clubId;
		}
		
		/** 모임 삭제 (논리 삭제) */
		@PostMapping("/delete")
		public String delete(@PathVariable Integer clubId) {
			clubManageService.deleteClub(clubId);
			return "redirect:/club/list";
		}
		
		/** 모임 삭제 복구 */
		@PostMapping("/restore")
		public String restore(@PathVariable Integer clubId) {
			clubManageService.restoreClub(clubId);
			return "redirect:/club/list";
		}
		
		/** 가입 승인 */
		@PostMapping("/approve")
		public String approve(@PathVariable Integer clubId, @RequestParam String memberId) {
			clubManageService.approveMember(clubId, memberId);
			clubManageService.leaveHistory(clubId, memberId, "SYSTEM", "APPROVE", "모임 가입 신청이 승인되었습니다.");
			return "redirect:/club/" + clubId + "/manage/requests";
		}
		
		/** 가입 거절 */
		@PostMapping("/reject")
		public String reject(@PathVariable Integer clubId, @RequestParam String memberId) {
			clubManageService.rejectMember(clubId, memberId);
			return "redirect:/club/" + clubId + "/manage/requests";
		}
		
		/** 멤버 권한 수정 */
		@PostMapping("/members/update-role")
		public String updateRole(@PathVariable Integer clubId,
								 @RequestParam String memberId,
								 @RequestParam String newRole) {
			String loginId = "user15"; // 테스트용
			clubManageService.updateMemberRole(clubId, loginId, memberId, newRole);
			return "redirect:/club/" + clubId + "/manage/members"; // 🚩 경로 확인 필요
		}
		
		/** 멤버 제명 */
		@PostMapping("/members/kick")
		public String kickMember(@PathVariable Integer clubId, @RequestParam String memberId) {
			String loginId = "user15"; // 테스트용
			clubManageService.kickMember(clubId, loginId, memberId);
			return "redirect:/club/" + clubId + "/manage/members"; // 🚩 경로 확인 필요
		}
	}
	
	
