package net.datasa.EnLink.community.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.member.entity.MemberEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)	// sent_at 생성 시간을 자동으로 관리하기 위해 필요
public class ChatMessageEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "msg_id")
	private Integer msgId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private ClubEntity club;	// 모임 ID (FK)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id")
	private MemberEntity sender;	// 발신자 ID(PK)
	
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;		// 메시지 내용 (TEXT)
	
	@CreatedDate
	@Column(name = "sent_at", updatable = false)
	private LocalDateTime sentAt;	// 발신 일시 (DATETIME)
}
