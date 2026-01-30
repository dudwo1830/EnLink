package net.datasa.EnLink.community.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/community/post")
public class PostViewController {
	
	// 게시글 목록 (주소: /community/post/list?clubId=1)
	@GetMapping("list")
	public String list(@RequestParam(name = "clubId", required = false, defaultValue = "1") Integer clubId,
					   Model model) {
		
		model.addAttribute("clubId", clubId);
		return "community/post/postList";
	}
	
	// 게시글 작성
	@GetMapping("write")
	public String writeForm(@RequestParam(name = "clubId", required = false, defaultValue = "1") Integer clubId, Model model) {
		
		// 1. 현재 게시판의 모임 ID 전달
		model.addAttribute("clubId", clubId);
		
		// 2. 작성자 정보 전달 (현재는 임시로 user01, 나중에 세션/시큐리티로 대체)
		model.addAttribute("memberId", "user01");
		
		return "community/post/postWrite";
	}
	
	// 게시글 상세보기
	@GetMapping("detail/{postId}")
	public String detailForm(@PathVariable(name = "postId") Integer postId, Model model) {
		model.addAttribute("postId", postId);
		return "community/post/postDetail";
	}
	
	// 게시글 수정
	@GetMapping("edit/{postId}")
	public String editForm(@PathVariable(name = "postId") Integer postId, Model model) {
		model.addAttribute("postId", postId);
		return "community/post/postEdit";
	}
}
