package net.datasa.EnLink.member.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
	public static final String FILED_RE_PASSWORD = "rePassword";

	@NotBlank
	@Size(min = 2, max = 20)
	private String memberId;

	@NotBlank
	@Size(min = 2, max = 20)
	private String name;

	@NotBlank
	@Size(min = 2, max = 100)
	private String password;
	@NotBlank
	@Size(min = 2, max = 100)
	private String rePassword;

	@NotBlank
	@Email
	@Size(min = 8, max = 50)
	private String email;

	@NotNull
	private LocalDate birth;
}