package net.datasa.EnLink.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("auth")
public class AuthController {
	private final String TEMPLATE_BASE = "auth/";

	@GetMapping("login")
	public String login() {
		return TEMPLATE_BASE + "login";
	}

}
