package net.datasa.EnLink.city.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.entity.RegionEntity;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegionCityResponse {
	private String region;
	private List<String> cities;

	public static RegionCityResponse from(RegionEntity region, List<CityEntity> cities) {
		return new RegionCityResponse(
				region.getNameLocal(),
				cities.stream().map(city -> city.getNameLocal()).toList());
	}
}
