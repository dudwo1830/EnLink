package net.datasa.EnLink.member.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.dto.request.MemberUpdateRequest;
import net.datasa.EnLink.member.service.MemberService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	/**
	 * 회원 수정 처리
	 * 
	 * @param memberId
	 * @param request
	 */
	@PatchMapping("{memberId}")
	public void update(@PathVariable String memberId, @RequestBody MemberUpdateRequest request) {
		memberService.update(request, memberId);
	}

	/**
	 * 회원 삭제 처리
	 * 
	 * @param memberId
	 */
	@DeleteMapping("{memberId}")
	public void delete(@PathVariable String memberId) {
		memberService.delete(memberId);
	}

}
