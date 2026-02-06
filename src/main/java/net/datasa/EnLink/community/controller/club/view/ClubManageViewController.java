	package net.datasa.EnLink.community.controller.club.view;
	
	import lombok.RequiredArgsConstructor;
	import net.datasa.EnLink.common.security.MemberDetails;
	import net.datasa.EnLink.community.dto.ClubDTO;
	import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
	import net.datasa.EnLink.community.dto.ClubMemberDTO;
	import net.datasa.EnLink.community.service.ClubManageService;
	import net.datasa.EnLink.community.service.ClubService;
	import org.springframework.data.domain.Page;
	import org.springframework.security.core.annotation.AuthenticationPrincipal;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	
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
				@RequestParam(value = "page", defaultValue = "0") int page,
				@AuthenticationPrincipal MemberDetails userDetails,
				Model model) {
			
			if (userDetails == null) return "redirect:/auth/login";
			
			ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, userDetails.getUsername());
			Page<ClubJoinRequestDTO> requestPage = clubManageService.getPendingRequestsPaging(clubId, page);
			
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("joinRequests", requestPage.getContent());
			model.addAttribute("paging", requestPage);
			model.addAttribute("clubId", clubId);
			
			return "club/manage/requests";
		}
		
		/** 모임 정보 수정 페이지 이동 */
		@GetMapping("/edit")
		public String edit(@PathVariable Integer clubId, Model model,
						   @AuthenticationPrincipal MemberDetails userDetails) {
			if (userDetails == null) return "redirect:/auth/login";
			
			ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, userDetails.getUsername());
			ClubDTO clubDTO = clubManageService.getClubForEdit(clubId);
			
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("clubDTO", clubDTO);
			
			return "club/manage/clubEdit";
		}
		
		/** 모임 삭제 페이지 이동 */
		@GetMapping("/delete")
		public String deleteForm(@PathVariable("clubId") Integer clubId,
								 @AuthenticationPrincipal MemberDetails userDetails,
								 Model model) {
			if (userDetails == null) return "redirect:/auth/login";
			
			ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, userDetails.getUsername());
			ClubDTO club = clubService.getClubDetail(clubId);
			
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("club", club);
			
			return "club/manage/clubDelete";
		}
		
		/** 멤버 관리 페이지 이동 */
		@GetMapping("/members")
		public String manageMembers(@PathVariable("clubId") Integer clubId,
									@AuthenticationPrincipal MemberDetails userDetails,
									Model model) {
			if (userDetails == null) return "redirect:/auth/login";
			
			ClubMemberDTO loginMember = clubManageService.getMemberInfo(clubId, userDetails.getUsername());
			
			model.addAttribute("members", clubManageService.getActiveMembers(clubId));
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("clubId", clubId);
			
			return "club/manage/members";
		}
	}
	
	
