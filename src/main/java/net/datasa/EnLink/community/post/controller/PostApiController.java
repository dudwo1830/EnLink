package net.datasa.EnLink.community.post.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {
	
	private final PostService postService;
	
	// 특정 모임의 게시글 목록 가져오기 (예: /api/posts?clubId=1)
	@GetMapping
	public List<PostEntity> getPostList(@RequestParam(name = "clubId") Integer clubId) {
		return postService.getPostsByClub(clubId);
	}
	
	// 게시글 상세 내용 가져오기 (예: /api/posts/10)
	@GetMapping("/{postId}")
	public PostEntity getPostDetail(@PathVariable(name = "postId") Integer postId) {
		return postService.getPostById(postId);
	}
	
	// 게시글 작성 및 저장
	@PostMapping // Post /api/posts
	public PostEntity createPost(@RequestBody PostEntity postEntity) {
		// 실제로는 현재 로그인한 사용자의 ID를 세션 등에서 가져와야 하지만,
		// 우선은 클라이언트가 보낸 데이터를 그대로 저장합니다.
		return postService.savePost(postEntity);
	}
	
	// 게시글 삭제
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable(name = "postId") Integer postId) {
		postService.deletePost(postId); // 서비스에 삭제 로직 구현 필요
		return ResponseEntity.ok().build();
	}
}
