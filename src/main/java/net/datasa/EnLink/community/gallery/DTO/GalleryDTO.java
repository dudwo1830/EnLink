package net.datasa.EnLink.community.gallery.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GalleryDTO {
	private Integer photoId;        // 사진 식별자 (INT)
	
	private Integer clubId;         // 모임 ID (INT)
	
	private String imageUrl;        // 사진 경로 (VARCHAR 500)
	
	private String isPublic;        // 공개 여부 (CHAR 1: 'Y' 또는 'N')
	
	private String memberId;        // 등록자 ID (VARCHAR 20)
	
	private LocalDateTime createdAt; // 등록 일시 (DATETIME)
}
