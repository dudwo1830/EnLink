package net.datasa.EnLink.member.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.service.MemberService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberRestController {
	private final MemberService memberService;

	/**
	 * 회원 가입 처리
	 * 
	 * @param request
	 */
	@PostMapping("")
	public void signup(@RequestBody MemberCreateRequest request) {
		memberService.create(request);
	}

}
