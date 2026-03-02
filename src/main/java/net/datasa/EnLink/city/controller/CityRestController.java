package net.datasa.EnLink.city.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.CityDetailResponse;
import net.datasa.EnLink.city.dto.response.RegionCityResponse;
import net.datasa.EnLink.city.service.CityService;
import net.datasa.EnLink.common.locale.LocaleType;

import java.util.List;
import java.util.Locale;



@Slf4j
@RestController
@RequestMapping("api/location/cities")
@RequiredArgsConstructor
public class CityRestController {
	private final CityService cityService;

	@GetMapping("all")
	public List<RegionCityResponse> getListAll() {
		return cityService.getListAll();
	}

	@GetMapping("")
	public List<CityDetailResponse> getCityList(
			@RequestParam(name = "regionId", required = false) Integer regionId, Locale locale) {
		String countryCode = LocaleType.from(locale).getCode();
		return cityService.getCityList(regionId, countryCode);
	}
}
