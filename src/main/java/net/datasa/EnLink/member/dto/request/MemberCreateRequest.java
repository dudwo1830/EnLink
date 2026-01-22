package net.datasa.EnLink.member.dto.request;

import java.time.LocalDate;

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
public class MemberCreateRequest {
	private String memberId;
	private String name;
	private String password;
	private String email;
	private LocalDate birth;
}