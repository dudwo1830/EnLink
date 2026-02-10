package net.datasa.EnLink.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubCreateRequest {
	private String name;
	private Integer topicId;
	private Integer cityId;
	private String description;
	private Integer maxMember;
	private String joinQuestion;
	private MultipartFile uploadFile;
}
