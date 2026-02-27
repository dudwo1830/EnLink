package net.datasa.EnLink.topic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TopicUpdateResponse {
	private int topicId;
	private String nameKo;
	private String nameJa;
}
