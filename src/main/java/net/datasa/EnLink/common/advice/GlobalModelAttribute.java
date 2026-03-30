package net.datasa.EnLink.common.advice;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;
import net.datasa.EnLink.topic.service.TopicService;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttribute {
	private final TopicService topicService;

	@ModelAttribute("topics")
	public List<TopicDetailResponse> topics(){
		return topicService.getListAll();
	}

	@ModelAttribute("currentURI")
	public String currentURI(HttpServletRequest request){
		return request.getRequestURI();
	}

	@ModelAttribute("locale")
	public String locale(){
		return LocaleContextHolder.getLocale().getLanguage();
	}
}
