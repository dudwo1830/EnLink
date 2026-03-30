package net.datasa.EnLink.city.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.city.dto.response.RegionDetailResponse;
import net.datasa.EnLink.city.service.RegionService;

@RestController
@RequestMapping("api/location/regions")
@RequiredArgsConstructor
public class RegionRestController {
	private final RegionService regionService;

	@GetMapping("")
	public List<RegionDetailResponse> getRegionList() {
		return regionService.getRegionList();
	}
}
