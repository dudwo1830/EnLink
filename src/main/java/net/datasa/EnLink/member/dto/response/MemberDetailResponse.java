package net.datasa.EnLink.member.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponse {
	private String memberId;
	private String name;
	private String email;
	private LocalDate birth;
	private List<TopicDetailResponse> topics;
	private String city;
}
