package net.datasa.EnLink.community.post.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
	private Integer postId;
	private String title;
	private String content;
	private Integer clubId;
	private String memberId;
	private LocalDateTime createdAt;
	private MultipartFile image;	// 등록할 때 받는 이미지 파일
	private String imageUrl;		// 화면에 보여줄 때 사용하는 이미지 경로(이름)
	private Boolean isNotice;
}
