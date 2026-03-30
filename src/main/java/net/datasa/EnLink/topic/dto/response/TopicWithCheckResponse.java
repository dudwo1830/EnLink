package net.datasa.EnLink.topic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicWithCheckResponse {
	private int topicId;
	private String name;
	private boolean checked;
}
