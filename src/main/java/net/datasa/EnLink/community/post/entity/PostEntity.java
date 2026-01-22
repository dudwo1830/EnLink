package net.datasa.EnLink.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_information")
@Getter @Setter
@Builder			// 빌더 패턴 사용 가능하게 해줌
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드 생성자 (Builder와 짝꿍)
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
	
//	// 1. 단순 ID가 아닌 '객체' 자체를 참조합니다.
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "club_id")
//	private ClubEntity club; // 모임 엔티티와 연결
//
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "member_id")
//	private MemberEntity member; // 회원 엔티티와 연결
	
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
