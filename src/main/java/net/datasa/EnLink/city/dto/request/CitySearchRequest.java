package net.datasa.EnLink.city.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CitySearchRequest {
	private Integer regionId;
	private String keyword;

}
