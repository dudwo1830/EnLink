package net.datasa.EnLink.community.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_information")
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = "clubId")
public class PostEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Integer postId;
	
	@Column(name = "club_id", nullable = false)
	private Integer clubId;
	
	@Column(name = "member_id", nullable = false, length = 20)
	private String memberId;
	
	@Column(nullable = false, length = 150)
	private String title;
	
	@Column(nullable = false, columnDefinition = "Text")
	private String content;
	
	@Column(name = "image_url", length = 500)
	private String imageUrl;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	// 저장 전 자동 시간 입력
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
}
