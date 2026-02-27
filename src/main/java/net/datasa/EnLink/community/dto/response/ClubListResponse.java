package net.datasa.EnLink.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubListResponse {
	private Integer clubId;
	private String name;
	private Integer topicId; // 주제 객체 (이름 포함)
	private Integer cityId;
	private String topicName;
	private String cityName;
	private Integer maxMember;
	private int currentMemberCount;
	private String imageUrl;
	private String description;
	private String status;
	
	public ClubListResponse(ClubEntity entity) {
		this.clubId = entity.getClubId();
		this.name = entity.getName();
		
		// 연관 엔티티가 있을 경우 ID 추출 (Null 체크 포함)
		if (entity.getTopic() != null) {
			// entity.getTopic()까지는 객체이므로 그 안의 '이름' 필드를 꺼내세요.
			this.topicName = entity.getTopic().getNameKo();
			this.topicId = entity.getTopic().getTopicId(); // ID도 필요하면 함께 채워주세요.
		}
		
		if (entity.getCity() != null) {
			// 영재 상의 방식처럼 "시/도 + 시/군/구" 합치기
			String region = (entity.getCity().getRegion() != null) ? entity.getCity().getRegion().getNameLocal() : "";
			String city = entity.getCity().getNameLocal();
			this.cityName = region + " " + city;
		}
		
		this.maxMember = entity.getMaxMember();
		this.currentMemberCount = entity.getMembers() != null ? entity.getMembers().size() : 0;
		this.imageUrl = entity.getImageUrl();
		this.description = entity.getDescription();
		this.status = entity.getStatus();
	}
}


