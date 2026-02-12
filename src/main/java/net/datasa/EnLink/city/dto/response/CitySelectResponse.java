package net.datasa.EnLink.city.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CitySelectResponse {
	private Integer cityId;
	private String nameLocal;
	private boolean selected;
}
