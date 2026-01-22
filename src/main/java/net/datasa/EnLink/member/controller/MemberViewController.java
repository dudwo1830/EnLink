package net.datasa.EnLink.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.service.MemberService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("members")
@RequiredArgsConstructor
public class MemberViewController {
	private final String TEMPLATE_PATH = "member/";
	private final MemberService memberService;

	/**
	 * 회원 가입 페이지
	 * 
	 * @return
	 */
	@GetMapping("")
	public String signup() {
		return TEMPLATE_PATH + "signup";
	}

	@GetMapping("{memberId}")
	public String read(@PathVariable String memberId, Model model) {
		MemberDetailResponse response = memberService.read(memberId);
		model.addAttribute("member", response);
		return TEMPLATE_PATH + "detail";
	}

}
