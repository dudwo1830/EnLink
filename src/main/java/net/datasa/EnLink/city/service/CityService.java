package net.datasa.EnLink.city.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.CityDetailResponse;
import net.datasa.EnLink.city.dto.response.RegionCityResponse;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.repository.CityRepository;
import net.datasa.EnLink.common.language.LanguageType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CityService {
	private final CityRepository cityRepository;

	public List<RegionCityResponse> getListAll(LanguageType lang) {
		List<CityEntity> cityEntities = cityRepository.findAllByCountryCode(lang.getCode());

		return cityEntities.stream()
				.collect(Collectors.groupingBy(
						city -> city.getRegion().getNameLocal(),
						LinkedHashMap::new,
						Collectors.mapping(CityEntity::getNameLocal, Collectors.toList())))
				.entrySet().stream()
				.map(e -> new RegionCityResponse(e.getKey(), e.getValue()))
				.toList();
	}

	public List<CityDetailResponse> getCityList(Integer regionId) {
		List<CityEntity> cities = cityRepository.findByRegion_regionId(regionId);
		return cities.stream()
				.map(city -> new CityDetailResponse(city.getCityId(), city.getNameLocal()))
				.toList();
	}

	public List<CityDetailResponse> getCityList(Integer regionId, String keyword, LanguageType lang) {
		List<CityEntity> cities = (regionId == null)
				? cityRepository.findByRegion_Country_CodeAndNameLocalContaining(lang.getCode(), keyword)
				: cityRepository.findByRegion_Country_CodeAndRegion_RegionIdAndNameLocalContaining(lang.getCode(), regionId,
						keyword);
		return cities.stream()
				.map(city -> new CityDetailResponse(city.getCityId(), city.getNameLocal()))
				.toList();
	}

}
