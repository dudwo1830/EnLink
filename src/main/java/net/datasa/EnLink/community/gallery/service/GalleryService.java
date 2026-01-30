package net.datasa.EnLink.community.gallery.service;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.gallery.DTO.GalleryDTO;
import net.datasa.EnLink.community.gallery.entity.GalleryEntity;
import net.datasa.EnLink.community.gallery.repository.GalleryRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryService {
	private final GalleryRepository galleryRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	
	// 공통 경로: C:/enlink_storage/
	@Value("${file.upload.path}")
	private String commonPath;
	
	// 사진 목록 조회
	public Page<GalleryDTO> getList(Integer clubId, int page) {
		Pageable pageable = PageRequest.of(page, 12, Sort.by("photoId").descending());
		Page<GalleryEntity> entityPage = galleryRepository.findByClub_ClubId(clubId, pageable);
		return entityPage.map(this::convertToDTO);
	}
	
	private GalleryDTO convertToDTO(GalleryEntity entity) {
		return GalleryDTO.builder()
				.photoId(entity.getPhotoId())
				.clubId(entity.getClub().getClubId())
				.imageUrl(entity.getImageUrl())
				.isPublic(entity.getIsPublic())
				.memberId(entity.getMember().getMemberId())
				.createdAt(entity.getCreatedAt())
				.build();
	}
	
	// 사진 업로드
	public void upload(MultipartFile image, Integer clubId, String memberId) {
		System.out.println("==== upload 메서드 시작 ====");
		System.out.println("받은 데이터: image=" + image.getOriginalFilename() + ", clubId=" + clubId + ", memberId=" + memberId);
		
		if (image == null || image.isEmpty()) {
			System.out.println("에러: 파일이 비어있음");
			throw new RuntimeException("파일이 없습니다.");
		}
		
		// 1. 파일 저장 (공통 경로 + gallery 하위 폴더)
		String savedFileName = saveFile(image);
		
		// 2. DB 저장
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("모임 정보를 찾을 수 없습니다."));
//		MemberEntity member = memberRepository.findById(memberId)
		MemberEntity member = memberRepository.findById("user01")
				.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
		
		GalleryEntity entity = GalleryEntity.builder()
				.club(club)
				.member(member)
				.imageUrl(savedFileName) // 저장된 파일명 (UUID_파일명)
				.isPublic("Y")
				.createdAt(LocalDateTime.now())
				.build();
		
		galleryRepository.save(entity);
	}
	
	private String saveFile(MultipartFile file) {
		// 경로가 잘 들어오는지 콘솔창에 찍어봅니다.
		System.out.println("현재 설정된 공통 경로: " + commonPath);
		
		// 공통 경로 아래에 gallery 폴더 지정
		String subPath = "gallery/";
		String fullPath = commonPath + subPath;
		
		System.out.println("최종 저장 경로: " + fullPath);

		File directory = new File(fullPath);
		if (!directory.exists()) directory.mkdirs(); // 폴더가 없으면 생성
		
		String originalFileName = file.getOriginalFilename();
		String savedFileName = UUID.randomUUID() + "_" + originalFileName;
		
		File dest = new File(fullPath, savedFileName);
		
		try {
			file.transferTo(dest);
		} catch (IOException e) {
			// 1. 여기에 추가: 에러의 상세 원인(StackTrace)을 콘솔에 출력합니다.
			e.printStackTrace();
			
			// 2. 그 다음 기존처럼 예외를 던집니다.
			throw new RuntimeException("파일 저장 오류", e);
		}
		return savedFileName;
	}
	
	// 사진 삭제
	public void delete(Integer photoId, String memberId) {
		// 1. DB에서 사진 정보 조회
		GalleryEntity entity = galleryRepository.findById(photoId)
				.orElseThrow(() -> new RuntimeException("사진을 찾을 수 없습니다."));
		
		// 2. 권한 확인 (등록한 사람과 현재 로그인한 사람이 일치하는지)
		if (!entity.getMember().getMemberId().equals(memberId)) {
			throw new RuntimeException("삭제 권한이 없습니다.");
		}
		
		// 3. 실제 파일 삭제 로직
		try {
			// commonPath: C:/enlink_storage/ , imageUrl: UUID_파일명.png
			Path filePath = Paths.get(commonPath + "gallery/" + entity.getImageUrl());
			Files.deleteIfExists(filePath); // 파일이 존재하면 삭제
		} catch (IOException e) {
			// 파일 삭제 실패 시 로그는 남기되, DB 삭제를 진행할지는 선택입니다.
			// 여기선 안전을 위해 로그만 남깁니다.
			System.err.println("물리 파일 삭제 실패: " + e.getMessage());
		}
		
		// 4. DB 데이터 삭제
		galleryRepository.delete(entity);
	}
}
