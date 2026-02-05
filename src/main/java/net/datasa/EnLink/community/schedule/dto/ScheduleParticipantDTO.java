package net.datasa.EnLink.community.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleParticipantDTO {
	// 엔티티 객체 대신, 식별할 수 있는 ID 값(Integer, String)만 담기
	private Integer schedId;
	private String memberId;
	private String memberName; // [추가] 화면에 표시할 이름
	private String status;		// 'JOIN', 'CANCEL'
	
	// 출력할 때 사용하기 위해 필드를 포함
	private LocalDateTime appliedAt;
}
