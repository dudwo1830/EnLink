package net.datasa.EnLink.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.dto.response.MemberUpdateResponse;
import net.datasa.EnLink.member.service.MemberService;

import org.springframework.web.bind.annotation.GetMapping;

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
	@GetMapping("signup")
	public String signup() {
		return TEMPLATE_PATH + "signup";
	}

	@GetMapping("me/edit")
	public String edit(@AuthenticationPrincipal MemberDetails member, Model model) {
		MemberUpdateResponse response = memberService.edit(member.getMemberId());
		model.addAttribute("member", response);
		return TEMPLATE_PATH + "edit";
	}

	/**
	 * 회원 정보 수정 페이지
	 * 
	 * @param memberId
	 * @param model
	 * @return
	 */
	@GetMapping("me")
	public String read(@AuthenticationPrincipal MemberDetails member, Model model) {
		MemberDetailResponse response = memberService.read(member.getMemberId());
		model.addAttribute("member", response);
		return TEMPLATE_PATH + "detail";
	}

	/**
	 * 관심사 관리 페이지
	 * 
	 * @param memberId
	 * @return
	 */
	@GetMapping("me/interest")
	public String interest(@AuthenticationPrincipal MemberDetails user, Model model) {
		return TEMPLATE_PATH + "interest";
	}

}
