package net.datasa.EnLink.community.post.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.dto.response.ClubMemberResponse;
import net.datasa.EnLink.community.post.repository.PostRepository;
import net.datasa.EnLink.community.service.ClubManageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("community/post")
@RequiredArgsConstructor
public class PostViewController {
	
	private final ClubManageService clubManageService;
	private final PostRepository postRepository;
	
	// 게시글 목록 (주소: /community/post/list?clubId=1)
	@GetMapping("list/{clubId}")
	public String list(@PathVariable Integer clubId,
					   @AuthenticationPrincipal MemberDetails member,
					   Model model) {
		// 1. 서비스에서 해당 모임 멤버인지 권한 체크
		model.addAttribute("loginMember", clubManageService.getMemberInfo(clubId, member.getMemberId()));
		
		model.addAttribute("clubId", clubId);
		
		return "community/post/postList";
	}
	
	// 게시글 작성
	@GetMapping("write")
	public String writeForm(@RequestParam(name = "clubId") Integer clubId,
							@AuthenticationPrincipal MemberDetails member,
							Model model) {
		
		model.addAttribute("clubId", clubId);
		
		// 로그인 안 된 경우도 방어
		if (member == null) {
			return "redirect:/community/post/list/" + clubId;
		}
		
		ClubMemberResponse clubMember =
				clubManageService.getMemberInfo(clubId, member.getMemberId());
		
		// 🔥 모임 멤버가 아니면 글쓰기 화면 못 들어오게 막기
		if (clubMember == null) {
			return "redirect:/community/post/list/" + clubId;
		}
		
		model.addAttribute("memberId", member.getMemberId());
		model.addAttribute("role", clubMember.getRole());
		
		return "community/post/postWrite";
	}
	
	// 게시글 상세보기
	@GetMapping("detail/{postId}")
	public String detailForm(@PathVariable Integer postId,
							 @AuthenticationPrincipal MemberDetails member,
							 Model model) {
		
		// 1️⃣ 로그인 안 되어 있으면 막기
		if (member == null) {
			return "redirect:/";
		}
		
		// 2️⃣ postId → clubId 찾기
		Integer clubId = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."))
				.getClub()
				.getClubId();
		
		// 3️⃣ 해당 모임의 멤버인지 확인
		ClubMemberResponse clubMember =
				clubManageService.getMemberInfo(clubId, member.getMemberId());
		
		// 4️⃣ 모임 멤버가 아니면 리스트로 돌려보내기
		if (clubMember == null) {
			return "redirect:/community/post/list/" + clubId;
		}
		
		// 5️⃣ 통과하면 상세페이지 보여주기
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
