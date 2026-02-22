package net.datasa.EnLink.common.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HomeController {

	/**
	 * 최초 접속 시 브라우저 설정에 따라 언어 지정
	 * @param request
	 * @return
	 */
	@GetMapping({ "/", "" })
	public String root(HttpServletRequest request) {
		Locale locale = request.getLocale();
		String lang = locale.getLanguage();

		if (!lang.equals("ja")) {
			lang = "ko";
		}

		return "redirect:/" + lang;
	}

	@GetMapping("{locale}")
	public String home(@PathVariable("locale") String locale) {
    if (!locale.equals("ko") && !locale.equals("ja")) {
        return "redirect:/ko";
    }
		return "home";
	}
}
