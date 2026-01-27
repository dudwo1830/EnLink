package net.datasa.EnLink.member.dto.response;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateResponse {
	private String memberId;
	private String name;
	private String password;
	private String email;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birth;
}
