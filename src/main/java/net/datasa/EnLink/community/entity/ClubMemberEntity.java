package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_members", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"club_id", "member_id"})
})
@Data
public class ClubMemberEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer cmId;
	
	@Column(name = "club_id", nullable = false)
	private Integer clubId;
	
	@Column(name = "member_id", nullable = false, length = 20)
	private String memberId;
	
	@Column(name = "role", columnDefinition = "ENUM('MEMBER', 'MANAGER', 'OWNER')")
	// @Enumerated(EnumType.STRING) <-- 에러가 나면 이 줄은 삭제하거나 주석 처리하세요!
	private String role;
	
	@Column(nullable = false)
	private String status = "PENDING"; // PENDING, ACTIVE, BANNED, EXIT
	
	private LocalDateTime joinedAt;
	
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime appliedAt;
	
	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
