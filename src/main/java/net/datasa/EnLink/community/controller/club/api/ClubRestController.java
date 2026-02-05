package net.datasa.EnLink.community.controller.club.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubSummaryResponse;
import net.datasa.EnLink.community.service.ClubService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/clubs")
@RequiredArgsConstructor
public class ClubRestController {

	private final ClubService clubService;

	@GetMapping("")
	public Slice<ClubSummaryResponse> getClubListBySlice(
			@PageableDefault(size = 5, sort = "clubId", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(name = "cityId") @Nullable Integer cityId,
			@RequestParam(name = "topicId") @Nullable Integer topicId,
			@RequestParam(name = "search") @Nullable String search) {
		return clubService.getClubListBySlice(pageable, cityId, topicId, search);
	}

}
