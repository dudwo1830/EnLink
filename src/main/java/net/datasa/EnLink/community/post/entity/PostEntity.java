package net.datasa.EnLink.community.post.entity;

import jakarta.persistence.*;
import lombok.*;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.member.entity.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_information")
@Getter @Setter
@Builder			// 빌더 패턴 사용 가능하게 해줌
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드 생성자 (Builder와 짝꿍)
@ToString(exclude = {"club", "member"})
public class PostEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Integer postId;
	
	
	// 단순 ID가 아닌 '객체' 자체를 참조
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private ClubEntity club; // 모임 엔티티와 연결

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberEntity member; // 회원 엔티티와 연결
	
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
	
	@Column(name = "is_notice", nullable = false)
	private Boolean isNotice = false; // 기본값은 일반글(false) (추가 후 DB에 컬럼이 생기도록 서버를 재시작하거나 SQL로 컬럼을 추가)
	
	// 저장 전 자동 시간 입력
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
