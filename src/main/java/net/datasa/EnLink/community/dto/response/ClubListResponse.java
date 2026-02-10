package net.datasa.EnLink.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubListResponse {
	private Integer clubId;
	private String name;
	private TopicDetailResponse topic; // 주제 객체 (이름 포함)
	private String cityName;
	private Integer maxMember;
	private int currentMemberCount;
	private String imageUrl;
	private String description;
	private String status;
}
