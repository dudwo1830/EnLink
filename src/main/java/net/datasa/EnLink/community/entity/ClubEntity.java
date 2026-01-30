package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clubs")
@Data
public class ClubEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "club_id")
	private Integer clubId;
	
	private Integer topicId;
	private Integer cityId;
	
	@Column(unique = true, nullable = false)
	private String name;
	
	private String description;
	
	@Builder.Default // 1. 빌더를 사용할 때도 이 기본값을 쓰겠다고 명시 (추가)
	@Column(nullable = false) // 2. DB 컬럼 설정 (기존 유지)
	private String imageUrl = "/images/default_club.jpg"; // 3. 실제 이미지 경로로 수정
	
	@Column(nullable = false)
	private Integer maxMember = 10;
	
	private String joinQuestion;
	
	@Builder.Default
	@Column(nullable = false)
	private String status = "ACTIVE";
	
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	private LocalDateTime deletedAt;
	
	@OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
	private List<ClubMemberEntity> members = new ArrayList<>();
}
