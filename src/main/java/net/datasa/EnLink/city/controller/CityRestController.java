package net.datasa.EnLink.city.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.CityDetailResponse;
import net.datasa.EnLink.city.dto.response.RegionCityResponse;
import net.datasa.EnLink.city.service.CityService;
import net.datasa.EnLink.common.language.LanguageType;

import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequestMapping("api/location/cities")
@RequiredArgsConstructor
public class CityRestController {
	private final CityService cityService;

	@GetMapping("all")
	public List<RegionCityResponse> getListAll(Locale locale) {
		return cityService.getListAll(LanguageType.from(locale));
	}

	@GetMapping("")
	public List<CityDetailResponse> getCityList(
			@RequestParam(required = false) @Nullable Integer regionId,
			@RequestParam String keyword,
			Locale locale) {
		return cityService.getCityList(regionId, keyword, LanguageType.from(locale));
	}

}
