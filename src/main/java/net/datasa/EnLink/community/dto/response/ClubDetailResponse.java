package net.datasa.EnLink.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDetailResponse {
	private Integer clubId;
	private String name;
	private TopicDetailResponse topic;
	private Integer cityId;
	private String cityName;
	private Integer maxMember;
	private int currentMemberCount;
	private String imageUrl;
	private String status;
	
	private String description;
	private String joinQuestion;
	private String role;
	private LocalDateTime createdAt;
	private String remainingTime;
}
