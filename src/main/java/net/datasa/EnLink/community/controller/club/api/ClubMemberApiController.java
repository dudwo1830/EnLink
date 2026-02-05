package net.datasa.EnLink.community.controller.club.api;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.ClubMemberHistoryDTO;
import net.datasa.EnLink.community.service.ClubMemberService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/club/{clubId}/member")
@RequiredArgsConstructor
public class ClubMemberApiController {
	
	private final ClubService clubService;
	private final ClubMemberService clubMemberService;
	
	/**
	 * 특정 멤버의 전체 활동 이력 조회
	 */
	@GetMapping("/{memberId}/history")
	public List<ClubMemberHistoryDTO> getMemberHistory(
			@PathVariable("clubId") Integer clubId,
			@PathVariable("memberId") String memberId) {
		return clubMemberService.getMemberImportantHistory(clubId, memberId);
	}
	
	/** 클럽 가입 신청 */
	@PostMapping("/apply")
	public ResponseEntity<String> apply(@PathVariable("clubId") Integer clubId,
										@RequestBody Map<String, String> request, // JSON 데이터를 받기 위함
								   		@AuthenticationPrincipal MemberDetails userDetails) {
		
		clubService.applyToClub(clubId, userDetails.getUsername(), request.get("answer"));
		return ResponseEntity.ok("가입 신청이 완료되었습니다.");
	}
	
	/** 클럽 가입 신청 취소 */
	@PostMapping("/cancel")
	public ResponseEntity<String> cancelApply(@PathVariable("clubId") Integer clubId,
											  @AuthenticationPrincipal MemberDetails userDetails) {
		
		clubService.cancelApplication(clubId, userDetails.getUsername());
		return ResponseEntity.ok("가입 신청이 취소되었습니다.");
	}
	
	/** 회원 탈퇴 */
	@PostMapping("/leave")
	public ResponseEntity<String> leave(@PathVariable Integer clubId,
								   @RequestBody Map<String, String> request,
								   @AuthenticationPrincipal MemberDetails userDetails) {
		
		clubMemberService.leaveClub(clubId, userDetails.getUsername(),request.get("description"));
		return ResponseEntity.ok("탈퇴 처리가 완료되었습니다.");
	}
}
