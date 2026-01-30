package net.datasa.EnLink.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
	private Integer clubId;			// 모임 고유번호
	private String name;           // 모임명
	private Integer topicId;       // 관심사 ID
	private Integer cityId;        // 지역 ID
	private String description;    // 모임 소개글
	private Integer maxMember;     // 최대 정원
	private String joinQuestion;   // 가입 질문
	private String imageUrl;
	private MultipartFile uploadFile; // 업로드된 파일을 담는 바구니
	private String status;
	private String remainingTime;
	
	private java.time.LocalDateTime createdAt; // 생성 일시
	private java.time.LocalDateTime deletedAt; // 삭제 요청 일시 (추가!)
	private int currentMemberCount;
	
	private String role;           // 추가: OWNER, MANAGER, MEMBER 등 권한 정보
	
}
