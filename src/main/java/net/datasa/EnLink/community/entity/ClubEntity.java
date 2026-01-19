package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
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
	
	@Column(nullable = false)
	private String imageUrl = "default_image_url";
	
	@Column(nullable = false)
	private Integer maxMember = 10;
	
	private String joinQuestion;
	
	@Column(nullable = false)
	private String status = "ACTIVE";
	
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	private LocalDateTime deletedAt;
}
