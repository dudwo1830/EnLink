package net.datasa.EnLink.community.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import net.datasa.EnLink.member.entity.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_participant")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ScheduleParticipantId.class) // 복합키 클래스 지정
public class ScheduleParticipantEntity {
	
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sched_id")
	private ScheduleEntity schedule;
	
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberEntity member;
	
	private String status; // 'JOIN', 'CANCEL'
	
	@Column(name = "applied_at", updatable = false)
	private LocalDateTime appliedAt;
	
	@PrePersist
	public void prePersist() {
		this.appliedAt = LocalDateTime.now();
		if (this.status == null) this.status = "JOIN"; // 기본값 설정
	}
}

