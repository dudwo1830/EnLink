package net.datasa.EnLink.community.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/community/schedule")
@RequiredArgsConstructor
public class ScheduleViewController {
	
	@GetMapping("/list/{clubId}")
	public String scheduleList(@PathVariable("clubId") Integer clubId, Model model) {
		// 화면에 clubId를 넘겨주어야 나중에 Ajax 통신이나 '일정 만들기' 시에 활용할 수 있습니다.
		model.addAttribute("clubId", clubId);
		
		// templates/community/scheduleList.html 로 연결
		return "community/schedule/scheduleList";
	}
}
