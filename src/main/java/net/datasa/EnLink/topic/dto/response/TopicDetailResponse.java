package net.datasa.EnLink.topic.dto.response;

import lombok.*;
import net.datasa.EnLink.topic.entity.TopicEntity;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicDetailResponse {
	private int topicId;
	private String name;
	
	public static TopicDetailResponse fromEntity(TopicEntity entity) {
		if (entity == null) return null; // 혹시라도 토픽이 없을 경우를 대비한 방어 코드
		
		return TopicDetailResponse.builder()
				.topicId(entity.getTopicId())
				.name(entity.getName())
				.build();
	}
}
