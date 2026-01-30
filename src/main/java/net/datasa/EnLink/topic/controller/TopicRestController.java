package net.datasa.EnLink.topic.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.topic.dto.request.TopicCreateRequest;
import net.datasa.EnLink.topic.dto.request.TopicUpdateRequest;
import net.datasa.EnLink.topic.dto.response.TopicWithCheckResponse;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;
import net.datasa.EnLink.topic.service.TopicService;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/topics")
@RequiredArgsConstructor
public class TopicRestController {
	private final TopicService topicService;

	/**
	 * 주제 추가
	 * 
	 * @param request
	 */
	@PostMapping("")
	public void create(@RequestBody TopicCreateRequest request) {
		topicService.create(request);
	}

	/**
	 * 주제 수정
	 * 
	 * @param topicId
	 * @param request
	 */
	@PatchMapping("{topicId}")
	public void update(@PathVariable int topicId, @RequestBody TopicUpdateRequest request) {
		topicService.update(topicId, request);
	}

	/**
	 * 주제 조회
	 * 
	 * @return
	 */
	@GetMapping("")
	public List<TopicDetailResponse> getListAll() {
		return topicService.getListAll();
	}

	/**
	 * 회원의 설정을 포함한 주제 조회
	 * 
	 * @return
	 */
	@GetMapping("me")
	public List<TopicWithCheckResponse> getCheckListAll(@AuthenticationPrincipal MemberDetails member) {
		return topicService.getCheckListAll(member.getMemberId());
	}
}
