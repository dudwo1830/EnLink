package net.datasa.EnLink.community.controller.club.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.request.ClubUpdateRequest;
import net.datasa.EnLink.community.service.ClubManageService;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/club/{clubId}/manage")
@RequiredArgsConstructor
public class ClubManageApiController {
	
	private final ClubManageService clubManageService;
	private final ClubService clubService;
	
	/**
	 * 모임 정보 수정 (Validation 및 상세 에러 메시지 포함)
	 */
	@PostMapping("/edit")
	public ResponseEntity<?> editClub(
			@PathVariable Integer clubId,
			@Valid @ModelAttribute ClubUpdateRequest request, // @Valid 추가
			BindingResult bindingResult,                     // 검증 결과 수집
			@AuthenticationPrincipal MemberDetails userDetails) {
		
		// 1. 유효성 검사 실패 시 에러 메시지 반환
		if (bindingResult.hasErrors()) {
			// 첫 번째 에러 메시지만 가져와서 반환 (팀장님 스타일대로 깔끔하게)
			String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
			return ResponseEntity.badRequest().body(errorMessage);
		}
		
		// 2. 비즈니스 로직 수행
		clubManageService.updateClub(clubId, request, userDetails.getUsername());
		
		return ResponseEntity.ok("모임 정보가 수정되었습니다.");
	}
	
	/**
	 * 모임명 중복검사
	 * */
		
		@GetMapping("/check-name-edit")
		public ResponseEntity<Map<String, Object>> checkNameForEdit(
				@RequestParam String name,
				@PathVariable Integer clubId) {
			
			Map<String, Object> result = new HashMap<>();
			
			// 1. 유효성 검사 (발리데이션 로직 재사용)
			if (name == null || name.trim().isEmpty()) {
				result.put("available", false);
				result.put("message", "모임명을 입력하세요");
				return ResponseEntity.ok(result);
			}
			
			if (name.length() < 2 || name.length() > 20) {
				result.put("available", false);
				result.put("message", "모임명은 2~20자 사이여야 합니다");
				return ResponseEntity.ok(result);
			}
			
			if (!name.matches("^[a-zA-Z0-9가-힣ぁ-んァ-ヶ一-龠\\s]+$")) {
				result.put("available", false);
				result.put("message", "모임명에 특수문자는 사용할 수 없습니다");
				return ResponseEntity.ok(result);
			}
			
			// 2. 중복 체크
			boolean isAvailable = clubService.isNameAvailableForEdit(name, clubId);
			result.put("available", isAvailable);
			result.put("message", isAvailable ? "사용 가능한 모임명입니다." : "이미 사용 중인 모임명입니다.");
			
			return ResponseEntity.ok(result);
		}
	
	/**
	 * 기본 모임 삭제 (논리 삭제) 7일 유예기간
	 */
	@PostMapping("/delete")
	public ResponseEntity<String> delete(@PathVariable Integer clubId,
										@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.deleteClub(clubId, userDetails.getUsername());
		return ResponseEntity.ok("모임이 삭제요청이 완료되었습니다.");
	}
	
	/**
	 * 모임 즉시(영구) 삭제
	 * - DB 레코드를 삭제하고 저장된 이미지 파일도 함께 제거함
	 */
	@DeleteMapping("/hard-delete")
	public ResponseEntity<String> hardDeleteClub(@PathVariable("clubId") Integer clubId,
												 @AuthenticationPrincipal MemberDetails userDetails) {
		log.info("모임 즉시 삭제 요청 접수 - 모임 ID: {}", clubId);
		
		try {
			clubManageService.hardDeleteClub(clubId, userDetails.getUsername());
			return ResponseEntity.ok("모임이 성공적으로 영구 삭제되었습니다.");
		} catch (Exception e) {
			log.error("모임 삭제 중 오류 발생: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("삭제 처리 중 서버 오류가 발생했습니다.");
		}
		
	}
	
	/**
	 * 모임 즉시 복구
	 */
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
	public ResponseEntity<String> kickMember(@PathVariable Integer clubId,
										@RequestParam String memberId,
										@RequestParam String description,
										@AuthenticationPrincipal MemberDetails userDetails) {
		clubManageService.kickMember(clubId, userDetails.getUsername(), memberId, description);
		return ResponseEntity.ok("해당 멤버를 제명 처리했습니다.");
	}
}
