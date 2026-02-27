package net.datasa.EnLink.city.service;

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.RegionDetailResponse;
import net.datasa.EnLink.city.entity.RegionEntity;
import net.datasa.EnLink.city.repository.RegionRepository;
import net.datasa.EnLink.common.locale.LocaleType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegionService {
	private final RegionRepository regionRepository;

	public List<RegionDetailResponse> getRegionList() {
		Locale locale = LocaleContextHolder.getLocale();
		String code = LocaleType.from(locale).getCode();
		List<RegionEntity> regions = regionRepository.findByCountry_codeOrderByNameLocalAsc(code);
		return regions.stream()
				.map(region -> new RegionDetailResponse(region.getRegionId(), region.getNameLocal()))
				.toList();
	}
}
