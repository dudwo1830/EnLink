package net.datasa.EnLink.member.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.dto.request.MemberUpdateRequest;
import net.datasa.EnLink.member.service.MemberService;
import net.datasa.EnLink.membertopic.dto.request.MemberTopicReplaceRequest;
import net.datasa.EnLink.membertopic.service.MemberTopicService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberRestController {
	private final MemberService memberService;
	private final MemberTopicService memberTopicService;

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
	 * @param member
	 * @param request
	 */
	@PatchMapping("me")
	public void update(@AuthenticationPrincipal MemberDetails member, @RequestBody MemberUpdateRequest request) {
		memberService.update(request, member.getMemberId());
	}

	/**
	 * 회원 삭제 처리
	 * 
	 * @param member
	 */
	@DeleteMapping("me")
	public void delete(@AuthenticationPrincipal MemberDetails member) {
		memberService.delete(member.getMemberId());
	}

	/**
	 * 회원-관심 주제 설정
	 *
	 * @param member
	 * @param topicIds
	 */
	@PutMapping("me/topics")
	public void replaceTopic(@AuthenticationPrincipal MemberDetails member,
			@RequestBody MemberTopicReplaceRequest request) {
		memberTopicService.replaceTopics(member.getMemberId(), request.getTopicIds());
	}
}
