package net.datasa.EnLink.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
	private Integer replyId;
	private Integer postId;
	private String memberId;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
