package net.datasa.EnLink.community.gallery.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/community/gallery")
@RequiredArgsConstructor
public class GalleryViewController {
	
	private final ClubRepository clubRepository;
	
	/**
	 * 갤러리 목록 페이지 이동
	 * 주소 예시: /community/gallery/1
	 */
	@GetMapping("/{clubId}")
	public String galleryList(@PathVariable("clubId") Integer clubId, Model model) {
		
		// 1. 해당 모임이 존재하는지 확인
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));
		
		// 2. HTML에 모임 정보를 전달 (JS의 clubId 변수에서 사용됨)
		model.addAttribute("club", club);
		
		// 3. templates/community/gallery/galleryList.html 열기
		// (폴더 구조에 맞춰 경로를 수정하세요)
		return "community/gallery/galleryList";
	}
}