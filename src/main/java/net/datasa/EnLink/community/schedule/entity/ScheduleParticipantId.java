package net.datasa.EnLink.community.schedule.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ScheduleParticipantEntity의 복합키 식별자 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleParticipantId implements Serializable {
	// 엔티티의 @Id 필드명과 똑같아야 함!
	private Integer schedule;	// ScheduleEntity의 PK 타입 (Integer)
	private String member;		// MemberEntity의 PK 타입 (String)
	}
