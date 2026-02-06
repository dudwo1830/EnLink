package net.datasa.EnLink.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClubJoinRequestDTO {
	private String memberId;
	private String memberName;
	private String answerText; // 가입 답변
	private String status;
	private LocalDateTime appliedAt;
	
	private String lastActionType;    // EXIT(탈퇴) 또는 BANNED(제명)
	private String lastDescription;   // "개인 사정", "부적절한 게시물" 등
	private LocalDateTime lastActionDate;
}
