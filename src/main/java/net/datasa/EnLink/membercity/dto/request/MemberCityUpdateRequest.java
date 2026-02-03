package net.datasa.EnLink.membercity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCityUpdateRequest {
	private String memberId;
	private Integer cityId;
}
