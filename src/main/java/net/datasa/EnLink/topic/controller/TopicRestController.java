package net.datasa.EnLink.topic.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.dto.TopicDTO;
import net.datasa.EnLink.topic.service.TopicService;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
	public void create(@RequestBody TopicDTO request) {
		topicService.create(request);
	}

	/**
	 * 주제 수정
	 * 
	 * @param topicId
	 * @param request
	 */
	@PatchMapping("{topicId}")
	public void update(@PathVariable int topicId, @RequestBody TopicDTO request) {
		topicService.update(topicId, request);
	}

}
