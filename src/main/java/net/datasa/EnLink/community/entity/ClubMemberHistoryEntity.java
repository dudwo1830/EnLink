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
	private Integer historyId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private ClubEntity club;
	
	// 이력의 대상이 되는 회원 (가입신청자, 제명당하는 자 등)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_member_id")
	private MemberEntity targetMember;
	
	// 이 행위를 수행한 회원 (신청자 본인, 혹은 승인/거절한 운영진)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_member_id")
	private MemberEntity actorMember;
	
	@Column(nullable = false, length = 50)
	private String actionType; // JOIN_REQUEST, JOIN_APPROVE, BANNED 등
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
