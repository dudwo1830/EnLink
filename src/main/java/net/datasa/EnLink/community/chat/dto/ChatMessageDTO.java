package net.datasa.EnLink.community.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
	private Integer clubId;    // 어느 모임 채팅방인지
	private String senderId;   // 누가 보냈는지
	private String senderName; // 발신자 이름 (화면 표시용)(선택사항)
	private String content;    // 메시지 내용
	private String type; // TEXT or IMAGE
	private String sentAt;     // 발신 일시 (문자열 포맷팅용)
	private boolean isDateDivider;	// 이 메시지가 날짜 구분선인지 여부
}
