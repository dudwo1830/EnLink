package net.datasa.EnLink.topic.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("admin/topics")
public class AdminTopicController {
	private final String TEMPLATE_BASE = "admin/";

	@GetMapping("")
	@PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
	public String topic() {
		return TEMPLATE_BASE + "topic";
	}

}
