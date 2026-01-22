package net.datasa.EnLink.community.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class PostDTO {
	private String title;
	private String content;
	private Integer clubId;
	private String memberId;
	private MultipartFile image;	// 등록할 때 받는 이미지 파일
	private String imageUrl;		// 화면에 보여줄 때 사용하는 이미지 경로(이름)
}
