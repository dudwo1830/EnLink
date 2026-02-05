package net.datasa.EnLink.community.controller.club.api;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.service.ClubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/club")
@RestController
public class ClubApiController {
	
	private final ClubService clubService;
	
	/** 모임 생성 처리  */
	@PostMapping("/create")
	public Integer create(@ModelAttribute ClubDTO clubDTO,
						  @AuthenticationPrincipal MemberDetails loginUser) {
		
			return clubService.createClub(clubDTO, loginUser.getUsername());
		}
}


