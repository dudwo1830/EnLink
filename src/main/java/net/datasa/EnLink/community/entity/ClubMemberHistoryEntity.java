package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_member_histories")
@Data
public class ClubMemberHistoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Integer historyId; // 이력 고유번호
	
	@Column(name = "club_id", nullable = false)
	private Integer clubId; // 모임 ID
	
	@Column(name = "target_member_id", nullable = false, length = 20)
	private String targetMemberId; // 대상 회원 ID (누가 대상인가)
	
	@Column(name = "actor_member_id", nullable = false, length = 20)
	private String actorMemberId; // 실행 회원 ID (누가 행동했나)
	
	@Column(name = "action_type", nullable = false, length = 50)
	private String actionType; // 행위 유형 (JOIN_REQUEST, ROLE_CHANGE 등)
	
	@Column(columnDefinition = "TEXT")
	private String description; // 상세 사유
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt; // 기록 일시
}
