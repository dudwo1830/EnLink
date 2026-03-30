package net.datasa.EnLink.community.controller.club.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.request.ClubCreateRequest;
import net.datasa.EnLink.community.dto.response.ClubListResponse;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/club")
@RestController
public class ClubApiController {
	
	private final ClubService clubService;
	
	@PostMapping("/create")
	public ResponseEntity<Map<String, Object>> create(
			@Valid @ModelAttribute ClubCreateRequest clubCreateDTO,
			BindingResult bindingResult,
			@AuthenticationPrincipal MemberDetails loginUser
	) {
		
		// ✅ 1️⃣ Validation 실패 처리
		if (bindingResult.hasErrors()) {
			
			Map<String, String> errors = new HashMap<>();
			
			bindingResult.getFieldErrors().forEach(error ->
					errors.put(error.getField(), error.getDefaultMessage())
			);
			
			return ResponseEntity.badRequest().body(Map.of(
					"success", false,
					"errors", errors
			));
		}
		
		// ✅ 2️⃣ 정상 처리
		Integer clubId = clubService.createClub(
				clubCreateDTO,
				loginUser.getUsername()
		);
		
		return ResponseEntity.ok(Map.of(
				"success", true,
				"clubId", clubId,
				"message", "모임이 생성되었습니다."
		));
	}
	
	@GetMapping("/check-name")
	@ResponseBody
	public Map<String, Object> checkName(@RequestParam String name) {
		
		Map<String, Object> result = new HashMap<>();
		
		// 1️⃣ 필수 입력 체크
		if (name == null || name.trim().isEmpty()) {
			result.put("available", false);
			result.put("message", "모임명을 입력해주세요");
			return result;
		}
		
		// 2️⃣ 길이 체크
		if (name.length() < 2 || name.length() > 20) {
			result.put("available", false);
			result.put("message", "모임명은 2~20자 사이여야 합니다");
			return result;
		}
		
		// 3️⃣ 허용 문자 체크
		if (!name.matches("^[a-zA-Z0-9가-힣ぁ-んァ-ヶ一-龠\\s]+$")) {
			result.put("available", false);
			result.put("message", "사용할 수 없는 이름입니다");
			return result;
		}
		
		// 4️⃣ 중복 체크
		boolean exists = clubService.existsByName(name);
		if (exists) {
			result.put("available", false);
			result.put("message", "이미 사용 중인 모임명입니다");
		} else {
			result.put("available", true);
			result.put("message", "사용 가능한 모임명입니다");
		}
		
		return result;
	}
	
	@GetMapping("/recommend")
	public ResponseEntity<List<ClubListResponse>> getRecommendedClubs(
			@AuthenticationPrincipal MemberDetails memberDetails) { // 1. 타입을 명시해야 합니다.
		
		// 로그인이 안 되어 있으면 4순위(전체 랜덤/인기순)
		if (memberDetails == null) {
			// 비로그인 시에도 ClubListResponse 리스트를 반환하도록 메서드명 통일 권장
			return ResponseEntity.ok(clubService.getRandomClubList());
		}
		
		// 로그인 유저의 PK 추출
		String memberId = memberDetails.getMemberId();
		
		// 개인화된 추천 리스트 반환
		return ResponseEntity.ok(clubService.getPersonalizedClubList(memberId));
	}
}


