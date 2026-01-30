package net.datasa.EnLink.community.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/club/{clubId}/post")
public class PostViewController {
	
	// 게시글 목록
	@GetMapping("list")
	public String list(){
		return "community/post/list";
	}
	
	// 게시글 작성
	@GetMapping("write")
	public String writeForm() {
		return "community/post/write";
	}
	
	// 게시글 상세보기
	@GetMapping("detail/{postId}")
	public String detailForm(@PathVariable(name = "postId") Integer postId, Model model) {
		model.addAttribute("postId", postId);
		return "community/post/detail";
	}
	
	// 게시글 수정
	@GetMapping("edit/{postId}")
	public String editForm(@PathVariable(name = "postId") Integer postId, Model model) {
		model.addAttribute("postId", postId);
		return "community/post/edit";
	}
}
