package net.datasa.EnLink.community.gallery.entity;

import jakarta.persistence.*;
import lombok.*;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.member.entity.MemberEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 입력을 위해 필요
@Table(name = "gallery_information")
public class GalleryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "photo_id" )
	private Integer photoId;
	
	// 모임 테이블 참조 (N:1)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id", nullable = false)
	private ClubEntity club;
	
	// 회원 테이블 참조 (N:1)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;
	
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;
	
	// 공개여부 반영 (기본값 'Y')
	@Column(name = "is_public", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private String isPublic;
	
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	// 저장 전 초기값 설정
	@PrePersist
	public void prePersist() {
		if (this.isPublic == null) this.isPublic = "Y";
	}
}
