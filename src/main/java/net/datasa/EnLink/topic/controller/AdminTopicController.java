package net.datasa.EnLink.topic.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.service.TopicService;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("admin/topics")
@RequiredArgsConstructor
public class AdminTopicController {
	private final String TEMPLATE_BASE = "admin/";
	private final TopicService topicService;

	@GetMapping("")
	@PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
	public String topic(Model model) {
		model.addAttribute("topics", topicService.getListAll());
		return TEMPLATE_BASE + "topic/list";
	}

}
