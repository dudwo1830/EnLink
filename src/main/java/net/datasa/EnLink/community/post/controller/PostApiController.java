package net.datasa.EnLink.community.post.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.PostDTO;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.service.PostService;
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
	
	// 특정 모임의 게시글 목록 가져오기 (예: /api/posts?clubId=1)
	@GetMapping
	public ResponseEntity<List<PostEntity>> getPostList(@RequestParam(name = "clubId") Integer clubId) {
		List<PostEntity> list = postService.getPostsByClub(clubId);
		return ResponseEntity.ok(list);
	}
	
	// 게시글 상세 내용 가져오기 (예: /api/posts/10)
	@GetMapping("/{postId}")
	public ResponseEntity<PostEntity> getPostDetail(@PathVariable(name = "postId") Integer postId) {
		PostEntity post = postService.getPostById(postId);
		return ResponseEntity.ok(post);
	}
	
	// 게시글 작성(응답 본문 없이 상태 코드로만 성공 전달)
	@PostMapping
	public ResponseEntity<Void> createPost(@ModelAttribute PostDTO postDTO) {
		try {
			postService.savePost(postDTO);
			return ResponseEntity.status(HttpStatus.CREATED).build();	// 201 Created
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	// 게시글 삭제
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable(name = "postId") Integer postId) {
		postService.deletePost(postId); // 서비스에 삭제 로직 구현 필요
		return ResponseEntity.noContent().build();	// 204 No Content(성공했지만 줄 데이터 없음)
	}
	
	// 게시글 수정
	@PutMapping("/{postId}")
	public ResponseEntity<Void> updatedPost(@PathVariable(name = "postId") Integer postId, @ModelAttribute PostDTO updatedPostDto) {
		try {
			// 서비스의 파라미터 타입(PostDTO)과 맞춤
			postService.updatePost(postId, updatedPostDto);
			return ResponseEntity.ok().build();	// 200 OK
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
}
