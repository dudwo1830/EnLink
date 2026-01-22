package net.datasa.EnLink.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Soft Delete
@SQLDelete(sql = "UPDATE members SET deleted_at = NOW() WHERE member_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MemberEntity {
	@Id
	@Column(name = "member_id", length = 20, nullable = false)
	private String memberId;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "password", length = 100, nullable = false)
	private String password;

	@Column(name = "email", length = 50, nullable = false, unique = true)
	private String email;

	@Column(name = "birth", nullable = false)
	private LocalDate birth;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private MemberStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}