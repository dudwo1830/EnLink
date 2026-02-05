package net.datasa.EnLink.community.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.member.entity.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_information")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer schedId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private ClubEntity club;
	
	private String title;
	private LocalDateTime eventDate;
	private String location;
	private Integer maxCapa;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	private MemberEntity admin;
}
