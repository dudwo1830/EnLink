package net.datasa.EnLink.city.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.CityDetailResponse;
import net.datasa.EnLink.city.dto.response.RegionCityResponse;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.repository.CityRepository;
import net.datasa.EnLink.common.locale.LocaleType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CityService {
	private final CityRepository cityRepository;

	public List<RegionCityResponse> getListAll() {
		Locale locale = LocaleContextHolder.getLocale();
		String code = LocaleType.from(locale).getCode();
		List<CityEntity> cityEntities = cityRepository.findAllByCountryCode(code);

		return cityEntities.stream()
				.collect(Collectors.groupingBy(
						city -> city.getRegion().getNameLocal(),
						LinkedHashMap::new,
						Collectors.mapping(CityEntity::getNameLocal, Collectors.toList())))
				.entrySet().stream()
				.map(e -> new RegionCityResponse(e.getKey(), e.getValue()))
				.toList();
	}

	@Cacheable(value = "cityListByRegionAndLocale", key = "#regionId + '_' + #countryCode")
	public List<CityDetailResponse> getCityList(Integer regionId, String countryCode) {
		List<CityEntity> cities = (regionId == null)
				? cityRepository.findByRegion_Country_CodeOrderByNameLocalAsc(countryCode)
				: cityRepository.findByRegion_RegionIdOrderByNameLocalAsc(regionId);
		return cities.stream()
				.map(city -> new CityDetailResponse(city.getCityId(), city.getNameLocal(),
						city.getRegion().getNameLocal() + " " + city.getNameLocal()))
				.toList();
	}

}
