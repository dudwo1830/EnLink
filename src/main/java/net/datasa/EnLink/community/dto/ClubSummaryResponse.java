package net.datasa.EnLink.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClubSummaryResponse {
	private Integer clubId;
	private String name;
	private String topicName;
	private String cityName;
	private String description;
	private String imageUrl;
}