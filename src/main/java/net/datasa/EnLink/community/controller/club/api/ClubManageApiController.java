package net.datasa.EnLink.community.controller.club.api;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.service.ClubManageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/{clubId}/manage")
@RequiredArgsConstructor
public class ClubManageApiController {
	
	private final ClubManageService clubManageService;
	
	/**
	 * 모임 정보 수정 (파일 업로드 포함)
	 */
	@PostMapping("/edit")
	public ResponseEntity<String> editClub(@PathVariable Integer clubId,
									  @ModelAttribute ClubDTO clubDTO,
									  @AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.updateClub(clubId, clubDTO, userDetails.getUsername());
		return ResponseEntity.ok("모임 정보가 수정되었습니다.");
	}
	
	/**
	 * 모임 삭제 (논리 삭제)
	 */
	@PostMapping("/delete")
	public ResponseEntity<String> delete(@PathVariable Integer clubId,
										@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.deleteClub(clubId, userDetails.getUsername());
		return ResponseEntity.ok("모임이 삭제되었습니다.");
	}
	
	@PostMapping("/restore")
	public ResponseEntity<String> restore(@PathVariable Integer clubId,
										 @AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.restoreClub(clubId, userDetails.getUsername());
		return ResponseEntity.ok("모임이 성공적으로 복구되었습니다.");
	}
	
	/**
	 * 가입 승인
	 */
	@PostMapping("/approve")
	public ResponseEntity<String> approve(@PathVariable Integer clubId,
									 @RequestParam String memberId,
									 @AuthenticationPrincipal MemberDetails userDetails) {
		
		clubManageService.approveMember(clubId, memberId, userDetails.getUsername());
		return ResponseEntity.ok("가입을 승인했습니다.");
	}
	
	/**
	 * 가입 거절
	 */
	@PostMapping("/reject")
	public ResponseEntity<String> reject(@PathVariable Integer clubId,
									@RequestParam String memberId,
									@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.rejectMember(clubId, memberId, userDetails.getUsername());
		return ResponseEntity.ok("가입 신청을 거절했습니다.");
	}
	
	/**
	 * 멤버 권한 수정
	 */
	@PostMapping("/members/update-role")
	public ResponseEntity<String> updateRole(@PathVariable Integer clubId,
										@RequestParam String memberId,
										@RequestParam String newRole,
										@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.updateMemberRole(clubId, userDetails.getUsername(), memberId, newRole);
		return ResponseEntity.ok("멤버 권한이 수정되었습니다.");
	}
	
	/**
	 * 멤버 제명
	 */
	@PostMapping("/members/kick")
	public ResponseEntity<?> kickMember(@PathVariable Integer clubId,
										@RequestParam String memberId,
										@RequestParam String description,
										@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.kickMember(clubId, userDetails.getUsername(), memberId, description);
		return ResponseEntity.ok("해당 멤버를 제명 처리했습니다.");
	}
}
