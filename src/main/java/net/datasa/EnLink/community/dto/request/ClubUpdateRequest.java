package net.datasa.EnLink.community.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubUpdateRequest {
	@NotNull(message = "수정할 모임 식별자가 없습니다.")
	private Integer clubId;
	
	@NotBlank(message = "모임명을 입력하세요")
	@Size(min = 2, max = 20, message = "모임명은 2~20자 사이여야 합니다")
	@Pattern(
			regexp = "^[a-zA-Z0-9가-힣ぁ-んァ-ヶ一-龠\\s]+$",
			message = "사용할 수 없는 이름입니다"
	)
	private String name;
	
	@NotNull(message = "관심사를 선택하세요")
	private Integer topicId;
	
	@NotNull(message = "지역을 선택하세요")
	private Integer cityId;
	
	@Size(max = 500, message = "소개글은 500자 이하로 입력하세요")
	private String description;
	
	@NotNull(message = "최대 인원을 선택하세요")
	@Min(value = 10, message = "최소 10명 이상이어야 합니다")
	@Max(value = 100, message = "최대 100명까지 가능합니다")
	private Integer maxMember;
	
	@Size(max = 100, message = "가입 질문은 100자 이하로 입력하세요")
	private String joinQuestion;
	
	private MultipartFile uploadFile;
	private boolean defaultImage;
}
