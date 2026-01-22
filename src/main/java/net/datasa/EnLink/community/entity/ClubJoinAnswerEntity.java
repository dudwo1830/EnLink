package net.datasa.EnLink.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_join_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubJoinAnswerEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "answer_id")
	private Integer answerId;
	
	@Column(name = "club_id")
	private Integer clubId;
	
	@Column(name = "member_id")
	private String memberId;
	
	@Column(name = "answer_text", nullable = false)
	private String answerText;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
