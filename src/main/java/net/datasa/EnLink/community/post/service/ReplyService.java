package net.datasa.EnLink.community.post.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.post.dto.ReplyDTO;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.post.entity.ReplyEntity;
import net.datasa.EnLink.community.post.repository.PostRepository;
import net.datasa.EnLink.community.post.repository.ReplyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyService {
	
	private final ReplyRepository replyRepository;
	private final PostRepository postRepository;
	
	/**
	 * 댓글 저장
	 */
	public void saveReply(ReplyDTO replyDTO) {
		// 1. 게시글 존재 여부 확인 (없으면 예외 던지기)
		PostEntity post = postRepository.findById(replyDTO.getPostId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		
		// 2. 내용 빈 값 체크 (예외 던지기 방식)
		if (replyDTO.getContent() == null || replyDTO.getContent().trim().isEmpty()) {
			throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
		}
		
		// 3. DTO -> Entity 변환
		ReplyEntity entity = ReplyEntity.builder()
				.post(post)
				.memberId(replyDTO.getMemberId())
				.content(replyDTO.getContent())
				.build();
		
		// 4. 저장 (@PrePersist에 의해 시간 자동 생성)
		replyRepository.save(entity);
	}
	
	/**
	 * 특정 게시글의 댓글 목록 조회 (Entity -> DTO 변환)
	 */
	public List<ReplyDTO> getRepliesByPostId(Integer postId) {
		List<ReplyEntity> entityList = replyRepository.findByPost_PostIdOrderByReplyIdAsc(postId);
		
		return entityList.stream().map(entity -> ReplyDTO.builder()
				.replyId(entity.getReplyId())
				.postId(entity.getPost().getPostId())
				.memberId(entity.getMemberId())
				.content(entity.getContent())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build()
		).collect(Collectors.toList());
	}
	
	/**
	 * 댓글 삭제
	 */
	public void deleteReply(ReplyDTO replyDTO) {
		ReplyEntity entity = replyRepository.findById(replyDTO.getReplyId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
		
		// 작성자 본인인지 확인하는 로직
		if (!entity.getMemberId().equals(replyDTO.getMemberId())) {
			throw new IllegalArgumentException("삭제 권한이 없습니다.");
		}
		
		replyRepository.delete(entity);
	}
	
	/**
	 * 댓글 수정
	 */
	public void updateReply(ReplyDTO replyDTO) {
		// 1. 수정할 원본 댓글 찾기
		ReplyEntity entity = replyRepository.findById(replyDTO.getReplyId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
		
		// 2. 권한 체크
		if (!entity.getMemberId().equals(replyDTO.getMemberId())) {
			throw new IllegalArgumentException("수정 권한이 없습니다.");
		}
		
		// 3. 내용 검증
		if (replyDTO.getContent() == null || replyDTO.getContent().trim().isEmpty()) {
			throw new IllegalArgumentException("수정할 내용을 입력해주세요.");
		}
		
		// 4. Dirty Checking으로 데이터 업데이트
		entity.setContent(replyDTO.getContent());
	}
}
