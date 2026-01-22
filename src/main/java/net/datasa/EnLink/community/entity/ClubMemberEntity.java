package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.member.entity.MemberEntity;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "club_members")
public class ClubMemberEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cm_id")
	private Integer cmId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private ClubEntity club;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberEntity member;
	
	@Column(name = "role", columnDefinition = "ENUM('OWNER', 'MANAGER', 'MEMBER')")
	private String role;
	
	@Column(name = "status")
	private String status; // PENDING, ACTIVE, BANNED, EXIT, REJECTED
	
	@Column(name = "joined_at")
	private LocalDateTime joinedAt;
	
	@Column(name = "applied_at", updatable = false)
	private LocalDateTime appliedAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@PrePersist
	protected void onCreate() {
		this.appliedAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
