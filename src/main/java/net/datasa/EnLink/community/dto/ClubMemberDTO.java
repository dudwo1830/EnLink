package net.datasa.EnLink.community.dto;


import lombok.*;
import net.datasa.EnLink.community.entity.ClubMemberEntity;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClubMemberDTO {
	private Integer cmId;       // ClubMember PK
	private String memberId;    // 회원 ID (Member PK)
	private String memberName;  // 회원 이름 (MemberEntity에서 가져옴)
	private String role;        // 권한 (OWNER, MANAGER...)
	private String status;      // 상태 (ACTIVE...)
	private LocalDateTime joinedAt;
	
	
	// 2. [해결용] 엔티티 덩어리 하나를 받는 생성자 (여기도 반드시 public!)
	public ClubMemberDTO(ClubMemberEntity entity) {
		// 사진 1의 에러 해결: .getMemberId()까지 써서 '글자'를 가져옵니다.
		this.cmId = entity.getCmId();
		this.memberId = entity.getMember().getMemberId();
		this.memberName = entity.getMember().getName();
		this.role = entity.getRole();
		this.status = entity.getStatus();
		this.joinedAt = entity.getJoinedAt();
	}
}



