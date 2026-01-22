package net.datasa.EnLink.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "members") // 영재님이 정한 테이블 이름
@Data
public class MemberEntity {
	@Id
	@Column(name = "member_id")
	private String memberId; // 영재님이 VARCHAR(20)으로 설정함
	
	private String name;
	
	@Column(name = "updated_at")
	private java.time.LocalDateTime updatedAt;
}
