package net.datasa.EnLink.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubJoinRequest {
	// 사용자는 '답변'만 딱 써서 보냅니다! 나머지는 서버가 로그인 정보로 처리해요.
	private String answerText;
}
