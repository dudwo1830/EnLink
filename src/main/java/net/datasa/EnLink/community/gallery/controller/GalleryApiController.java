package net.datasa.EnLink.community.gallery.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.gallery.DTO.GalleryDTO;
import net.datasa.EnLink.community.gallery.service.GalleryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryApiController {
	
	private final GalleryService galleryService;
	
	/**
	 * 사진 목록 조회 (목록/더보기)
	 * @param clubId 모임 ID
	 * @param page 현재 페이지 번호 (0부터 시작)
	 */
	@GetMapping("{clubId}")
	public ResponseEntity<Page<GalleryDTO>> getGalleryList(
			@PathVariable("clubId") Integer clubId,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		
		Page<GalleryDTO> list = galleryService.getList(clubId, page);
		return ResponseEntity.ok(list);
	}
	
	/**
	 * 사진 업로드
	 * @param user 현재 로그인한 사용자 정보 (나중에 통합시 사용)
	 */
	@PostMapping("/upload")
	public ResponseEntity<String> uploadImage(
			@RequestParam("image") MultipartFile image,
			@RequestParam("clubId") Integer clubId,
			@AuthenticationPrincipal UserDetails user) { // 로그인 통합을 고려한 스텁
		
		// TODO: 로그인 기능 병합 전까지는 테스트용 아이디를 수동으로 입력하거나
		// 시큐리티 설정 전이라면 String memberId = "testUser"; 로 대체 가능
		String memberId = (user != null) ? user.getUsername() : "user01";
		
		try {
			galleryService.upload(image, clubId, memberId);
			return ResponseEntity.ok("업로드 성공");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("업로드 실패: " + e.getMessage());
		}
	}
	
	@DeleteMapping("/{photoId}")
	public ResponseEntity<String> deleteImage(
			@PathVariable("photoId") Integer photoId,
			@AuthenticationPrincipal UserDetails user) {
		
		try {
			// 현재 로그인한 사용자 정보 가져오기 (없으면 테스트용 스텁)
			String memberId = (user != null) ? user.getUsername() : "user01";
			
			galleryService.delete(photoId, memberId);
			return ResponseEntity.ok("사진이 삭제되었습니다.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("삭제 실패: " + e.getMessage());
		}
	}
	
}
