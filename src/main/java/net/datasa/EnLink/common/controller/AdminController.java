package net.datasa.EnLink.common.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("admin")
public class AdminController {
	private final String TEMPLATE_BASE = "admin/";

	@GetMapping("topics")
	@PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
	public String topic() {
		return TEMPLATE_BASE + "topic";
	}

}
