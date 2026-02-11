package net.datasa.EnLink.community.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
	private Integer schedId;
	private Integer clubId;
	private String title;
	
	// 1. 들어올 때 (HTML -> Server) : T가 포함된 형식을 읽어들임
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	// 2. 나갈 때 (Server -> Ajax) : T를 빼고 공백으로 예쁘게 보냄
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime eventDate;
	
	private String location;
	private Integer maxCapa;
	private String adminId;
	
	// 추가로 필요한 정보 (참여 인원수 등)
	private Long currentParticipants;
	
	private boolean isAdmin; // (또는 admin)
	
	private boolean isJoined; // 내가 참여 중이면 true, 아니면 false
}
