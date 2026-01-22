package net.datasa.EnLink.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("members")
@RequiredArgsConstructor
public class MemberViewController {
	private final String TEMPLATE_PATH = "member/";

	/**
	 * 회원 가입 페이지
	 * 
	 * @return
	 */
	@GetMapping("")
	public String signup() {
		return TEMPLATE_PATH + "signup";
	}
}
