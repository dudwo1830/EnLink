package net.datasa.EnLink.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 출력용(상세조회)
 */
public class PostDetailResponseDTO {
	private Integer postId;
	private String title;
	private String content;
	private Integer clubId;
	private String memberId;
	private String createdAt; // 렌더링용 String으로 변환
	private String imageUrl;  // 보여주기용
	private Boolean isNotice;
	
	// 권한 제어 필드 추가
	private boolean canEdit;   // 작성자 본인인가?
	private boolean canDelete; // 작성자 또는 운영진인가?
}
