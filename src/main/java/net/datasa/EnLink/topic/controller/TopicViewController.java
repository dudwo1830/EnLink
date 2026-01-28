package net.datasa.EnLink.topic.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.dto.TopicDTO;
import net.datasa.EnLink.topic.service.TopicService;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TopicViewController {
	private final String TEMPLATE_PATH = "admin/topic/";
	private final TopicService topicService;

	/**
	 * 주제 리스트 조회
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("")
	public String list(Model model) {
		List<TopicDTO> topics = topicService.getListAll();
		model.addAttribute("topics", topics);
		return TEMPLATE_PATH + "list";
	}

}
