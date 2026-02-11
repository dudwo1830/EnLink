package net.datasa.EnLink.community.controller.club.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.request.ClubCreateRequest;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/club")
@RestController
public class ClubApiController {
	
	private final ClubService clubService;
	
	@PostMapping("/create")
	public ResponseEntity<Map<String, Object>> create(@Valid @ModelAttribute ClubCreateRequest clubCreateDTO,
													  @AuthenticationPrincipal MemberDetails loginUser) {
		Integer clubId = clubService.createClub(clubCreateDTO, loginUser.getUsername());
		Map<String, Object> result = Map.of("clubId", clubId, "message", "모임이 생성되었습니다.");
		return ResponseEntity.ok(result);
	}
}


