package net.datasa.EnLink.city.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.RegionDetailResponse;
import net.datasa.EnLink.city.entity.RegionEntity;
import net.datasa.EnLink.city.repository.RegionRepository;
import net.datasa.EnLink.common.language.LanguageType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegionService {
	private final RegionRepository regionRepository;

	public List<RegionDetailResponse> getRegionList(LanguageType lang) {
		List<RegionEntity> regions = regionRepository.findByCountry_code(lang.getCode());
		return regions.stream()
				.map(region -> new RegionDetailResponse(region.getRegionId(), region.getNameLocal()))
				.toList();
	}
}
