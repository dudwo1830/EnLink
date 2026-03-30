package net.datasa.EnLink.community.dto.response;


import lombok.*;
import net.datasa.EnLink.community.entity.ClubMemberEntity;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClubMemberResponse {
	private Integer cmId;       // ClubMember PK
	private String memberId;    // 회원 ID (Member PK)
	private String memberName;  // 회원 이름 (MemberEntity에서 가져옴)
	private String role;        // 권한 (OWNER, MANAGER...)
	private String status;      // 상태 (ACTIVE...)
	private LocalDateTime joinedAt;
	
	
	public ClubMemberResponse(ClubMemberEntity entity) {
		this.cmId = entity.getCmId();
		this.memberId = entity.getMember().getMemberId();
		this.memberName = entity.getMember().getName();
		this.role = entity.getRole();
		this.status = entity.getStatus();
		this.joinedAt = entity.getJoinedAt();
	}
}



