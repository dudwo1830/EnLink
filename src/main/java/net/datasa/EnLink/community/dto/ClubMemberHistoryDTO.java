package net.datasa.EnLink.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberHistoryDTO {
	private Integer historyId;
	private Integer clubId;
	
	private String targetMemberId;
	private String targetMemberName; // 화면 표시용
	
	private String actorMemberId;
	private String actorMemberName;  // 누가 처리했는지 표시용
	
	private String actionType;
	private String description;
	private LocalDateTime createdAt;
	
	// Entity -> DTO 변환 생성자
	public static ClubMemberHistoryDTO fromEntity(ClubMemberHistoryEntity entity) {
		return ClubMemberHistoryDTO.builder()
				.historyId(entity.getHistoryId())
				.clubId(entity.getClub().getClubId())
				.targetMemberId(entity.getTargetMember().getMemberId())
				.targetMemberName(entity.getTargetMember().getName())
				.actorMemberId(entity.getActorMember().getMemberId())
				.actorMemberName(entity.getActorMember().getName())
				.actionType(entity.getActionType())
				.description(entity.getDescription())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}
