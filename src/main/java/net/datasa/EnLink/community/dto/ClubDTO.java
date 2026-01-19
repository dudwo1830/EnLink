package net.datasa.EnLink.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
	private String name;           // 모임명
	private Integer topicId;       // 관심사 ID
	private Integer cityId;        // 지역 ID
	private String description;    // 모임 소개글
	private Integer maxMember;     // 최대 정원
	private String joinQuestion;   // 가입 질문
	// image_url은 기본값이 있으므로 우선 제외하거나 필요시 추가
}
