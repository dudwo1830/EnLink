package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.member.entity.MemberEntity;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "club_member_histories")
@Data
public class ClubMemberHistoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Integer historyId;
	
	// 1. 모임 엔티티와 연결 (어떤 모임에서 일어난 일인가)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id", nullable = false)
	private ClubEntity club;
	
	// 2. 대상 회원 (수정된 SQL에 따라 MemberEntity를 직접 참조)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_member_id", nullable = false)
	private MemberEntity targetMember;
	
	// 3. 실행 회원 (행위자)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_member_id", nullable = false)
	private MemberEntity actorMember;
	
	@Column(name = "action_type", nullable = false, length = 50)
	private String actionType;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
