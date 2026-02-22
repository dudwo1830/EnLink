package net.datasa.EnLink.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClubSummaryResponse {
	private Integer clubId;
	private String name;
	private String topicName;
	private String cityName;
	private String description;
	private String imageUrl;
	private int currentMemberCount;
	private int maxMemberCount;

	public ClubSummaryResponse(
			Integer clubId,
			String name,
			String topicName,
			String regionName,
			String cityName,
			String imageUrl,
			String description,
			Long currentMemberCount,
			Integer maxMemberCount) {
		this.clubId = clubId;
		this.name = name;
		this.topicName = topicName;
		this.cityName = regionName + " " + cityName;
		this.imageUrl = imageUrl;
		this.description = description;
		this.currentMemberCount = currentMemberCount.intValue();
		this.maxMemberCount = maxMemberCount;
	}
}