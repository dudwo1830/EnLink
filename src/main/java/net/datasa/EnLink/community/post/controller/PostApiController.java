package net.datasa.EnLink.community.post.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.post.dto.PostDTO;
import net.datasa.EnLink.community.post.dto.ReplyDTO;
import net.datasa.EnLink.community.post.service.PostService;
import net.datasa.EnLink.community.post.service.ReplyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor	// 서비스 부품을 가져오기 위한 생성자 자동 생성
public class PostApiController {
	
	private final PostService postService;
	private final ReplyService replyService;
	
	// 특정 모임의 게시글 목록 가져오기 (예: /api/posts?clubId=1)
	@GetMapping
	public ResponseEntity<Page<PostDTO>> getPostList(
			@RequestParam(name = "clubId") Integer clubId,
			@RequestParam(name = "searchType", required = false, defaultValue = "all") String searchType,
			@RequestParam(name = "searchKeyword", required = false) String searchKeyword,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		// 서비스 호출 시 검색 조건과 페이지 번호를 모두 전달
		Page<PostDTO> postPage = postService.getPostList(clubId, searchType, searchKeyword, page);
		
		// Page 객체 자체를 리턴
		return ResponseEntity.ok(postPage);
	}
	
	// 게시글 상세 내용 가져오기 (예: /api/posts/10)
	@GetMapping("/{postId}")
	public ResponseEntity<PostDTO> getPostDetail(@PathVariable(name = "postId") Integer postId) {
		PostDTO postDTO = postService.getPostById(postId);
		return ResponseEntity.ok(postDTO);
	}
	
	// 게시글 작성(응답 본문 없이 상태 코드로만 성공 전달)
	@PostMapping
	public ResponseEntity<?> createPost(@ModelAttribute PostDTO postDTO) {
		try {
			postService.savePost(postDTO);
			return ResponseEntity.status(HttpStatus.CREATED).build();    // 201 Created
		} catch (IllegalArgumentException e) {
			// [수정] 서비스에서 던진 "제목을 입력해주세요" 메시지를 400 에러와 함께 보냄
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 중 오류가 발생했습니다.");
		}
	}
	
	// 게시글 삭제
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable(name = "postId") Integer postId) {
		postService.deletePost(postId); // 서비스에 삭제 로직 구현 필요
		return ResponseEntity.noContent().build();    // 204 No Content(성공했지만 줄 데이터 없음)
	}
	
	// 게시글 수정
	@PutMapping("/{postId}")
	public ResponseEntity<?> updatedPost(@PathVariable(name = "postId") Integer postId, @ModelAttribute PostDTO updatedPostDto) {
		try {
			// 서비스의 파라미터 타입(PostDTO)과 맞춤
			postService.updatePost(postId, updatedPostDto);
			return ResponseEntity.ok().build();    // 200 OK
		} catch (IllegalArgumentException e) {
			// [수정] 수정 시 빈 값 검증 실패 처리
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 수정 중 오류가 발생했습니다.");
		}
	}
	
	// 댓글 저장
	@PostMapping("/{postId}/replies")
	public ResponseEntity<?> saveReply(
			@PathVariable(name = "postId") Integer postId,
			@RequestParam(name = "content") String content
			/*@AuthenticationPrincipal AuthenticatedUser user 로그인 완성될 때까지 일단 제외*/
	) {
		try {
			// 실제로는 세션이나 Security에서 memberId를 가져와야 합니다.
			// 현재는 테스트를 위해 "testUser"로 고정하거나 파라미터로 받으세요.
			String memberId = "user01";
			
			// ReplyDTO를 만들어 서비스로 전달 (서비스 규격에 맞춤)
			ReplyDTO replyDTO = ReplyDTO.builder()
					.postId(postId)
					.memberId(memberId)
					.content(content)
					.build();
			
			
			replyService.saveReply(replyDTO);
			return ResponseEntity.ok("댓글이 등록되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
		}
	}
	
	// 댓글 삭제
	@DeleteMapping("/replies/{replyId}")
	public ResponseEntity<?> deleteReply(@PathVariable(name = "replyId") Integer replyId) {
		try {
			// 테스트용 아이디 (DB에 실제 존재하는 아이디여야 함)
			String memberId = "user01";
			
			// 서비스 규격에 맞게 DTO 생성
			ReplyDTO replyDTO = ReplyDTO.builder()
					.replyId(replyId)
					.memberId(memberId)
					.build();
			
			replyService.deleteReply(replyDTO);
			return ResponseEntity.ok().build(); // 200 OK
		} catch (IllegalArgumentException e) {
			// 권한 없음이나 댓글 없음 에러 메시지 전달
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류가 발생했습니다.");
		}
	}
	
	// 댓글 수정
	@PutMapping("/replies/{replyId}")
	public ResponseEntity<?> updateReply(
			@PathVariable(name = "replyId") Integer replyId,
			@RequestParam(name = "content") String content) {
		try {
			String memberId = "user01";
			
			// 서비스 규격에 맞게 DTO 생성
			ReplyDTO replyDTO = ReplyDTO.builder()
					.replyId(replyId)
					.memberId(memberId)
					.content(content)
					.build();
			
			replyService.updateReply(replyDTO);
			return ResponseEntity.ok().build(); // 200 OK
		} catch (IllegalArgumentException e) {
			// 빈 값 검증 실패 등의 메시지 전달
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류가 발생했습니다.");
		}
	}
	
	// 댓글 목록
	@GetMapping("/{postId}/replies")
	public ResponseEntity<List<ReplyDTO>> getReplies(@PathVariable(name = "postId") Integer postId) {
		List<ReplyDTO> replies = replyService.getRepliesByPostId(postId);
		return ResponseEntity.ok(replies);
	}
}
