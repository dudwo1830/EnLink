package net.datasa.EnLink.community.schedule.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.security.MemberDetails;
import net.datasa.EnLink.community.service.ClubManageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/community/schedule")
@RequiredArgsConstructor
public class ScheduleViewController {
	private final ClubManageService clubManageService;
	
	@GetMapping("/list/{clubId}")
	public String scheduleList(@PathVariable("clubId") Integer clubId, @AuthenticationPrincipal MemberDetails member, Model model) {
		
		// 1. 서비스에서 해당 모임 멤버인지 권한 체크
		model.addAttribute("loginMember", clubManageService.getMemberInfo(clubId, member.getMemberId()));
		
		// 화면에 clubId를 넘겨주어야 나중에 Ajax 통신이나 '일정 만들기' 시에 활용할 수 있습니다.
		model.addAttribute("clubId", clubId);
		// templates/community/scheduleList.html 로 연결
		return "community/schedule/scheduleList";
	}
}
