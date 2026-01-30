package net.datasa.EnLink.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reply")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reply_id")
	private Integer replyId;
	
	// 어떤 게시글의 댓글인지 연결!!
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private PostEntity post;
	
	@Column(name = "member_id", nullable = false, length = 20)
	private String memberId; // 작성자ID
	
	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content; // 댓글 내용
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
