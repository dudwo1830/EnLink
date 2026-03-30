package net.datasa.EnLink.community.post.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.post.dto.PostDTO;
import net.datasa.EnLink.community.post.dto.PostDetailResponseDTO;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.post.repository.PostRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
	
	private final PostRepository postRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final ClubMemberRepository clubMemberRepository;
	
	// application.properties에서 경로를 가져옴
	@Value("${file.upload.path}")
	private String rootPath;
	
	// 게시판 전용 하위 폴더명
	private final String POST_SUB_DIR = "post/";
	
	// 실제 저장할 전체 경로 조합 (root + sub)
	private String getFullUploadPath() {
		return rootPath + POST_SUB_DIR;
	}
	
	// 1. 게시글 저장하기 (PostDTO를 받도록 수정)
	@Transactional
	public void savePost(PostDTO postDTO) throws IOException {
		validatePost(postDTO);
		
		// 모임 소속 여부 및 역할 확인 추가
		ClubMemberEntity clubMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(postDTO.getClubId(), postDTO.getMemberId())
				.orElseThrow(() -> new IllegalArgumentException("해당 모임의 멤버가 아닙니다."));
		
		// 가입 상태 확인
		if (!"ACTIVE".equals(clubMember.getStatus())) {
			throw new IllegalArgumentException("가입 대기 중이거나 탈퇴한 멤버입니다.");
		}
		
		// 공지사항 등록 권한 체크 (OWNER, MANAGER만 가능)
		if (postDTO.getIsNotice() != null && postDTO.getIsNotice()) {
			boolean isManager = "OWNER".equals(clubMember.getRole()) || "MANAGER".equals(clubMember.getRole());
			
			if (!isManager) {
				throw new IllegalArgumentException("공지사항은 모임 운영진(방장/매니저)만 작성할 수 있습니다.");
			}
		}
		
		// 파일 저장 로직
		String savedFileName = null;
		// 추가 이미지가 있다면 물리적으로 저장하는 로직
		if (postDTO.getImage() != null && !postDTO.getImage().isEmpty()) {
			MultipartFile imageFile = postDTO.getImage();
			
			// 중복 방지 이름 생성: uuid_원본이름.jpg
			String uuid = UUID.randomUUID().toString();
			savedFileName = uuid + "_" + imageFile.getOriginalFilename();
			
			// [추가] 폴더 경로 조합 및 폴더 생성
			File uploadDir = new File(getFullUploadPath());
			if (!uploadDir.exists()) {
				uploadDir.mkdirs(); // 폴더가 없으면 생성
			}
			
			// 파일 저장 실행
				File saveFile = new File(uploadDir, savedFileName);
				imageFile.transferTo(saveFile);
		}
		
		ClubEntity club = clubRepository.findById(postDTO.getClubId()).orElseThrow();
		MemberEntity member = memberRepository.findById(postDTO.getMemberId()).orElseThrow();
		
		// DTO의 데이터를 Entity로 옮겨담기
		PostEntity postEntity = PostEntity.builder()
				.club(club)
				.member(member)
				.title(postDTO.getTitle())
				.content(postDTO.getContent())
				.isNotice(postDTO.getIsNotice() != null && postDTO.getIsNotice())
				.imageUrl(savedFileName)
				.build();
		
		postRepository.save(postEntity);
	}
	
	// [추가] 공통 검증 메서드 (중복 코드를 줄이기 위해 별도로 뺌)
	private void validatePost(PostDTO dto) {
		if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("제목을 입력해주세요.");
		}
		if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
			throw new IllegalArgumentException("내용을 입력해주세요.");
		}
	}
	
	// 2. 특정 모임의 모든 게시글 가져오기
	public Page<PostDTO> getPostsByClub(Integer clubId, int page) {
		// 정렬 조건 설정
		Sort sort = Sort.by(
				Sort.Order.desc("isNotice"),
				Sort.Order.desc("postId")
		);
		
		// 페이지 설정 (0번 페이지부터 시작, 한 페이지에 10개)
		Pageable pageable = PageRequest.of(page, 10, sort);
		
		// Repository 호출 (Page<PostEntity> 리턴)
		Page<PostEntity> entities = postRepository.findByClub_ClubId(clubId, pageable);
		
		// Page 객체의 .map() 메서드를 사용해 DTO로 변환
		return entities.map(this::convertToDTO);
	}
	
	// 3. 게시글 상세 내용 하나 가져오기 (PostDetailResponseDTO 리턴하도록 수정)
	public PostDetailResponseDTO getPostDetail(Integer postId, String currentMemberId) {
		// 1. DB에서 엔티티 조회
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));
		
		// 2. 운영진 여부 조회 (권한 계산용)
		// 로그인하지 않은 경우(null)는 일반 멤버로 처리
		boolean isManager = false;
		if (currentMemberId != null) {
			isManager = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(
							post.getClub().getClubId(), currentMemberId)
					.map(cm -> "OWNER".equals(cm.getRole()) || "MANAGER".equals(cm.getRole()))
					.orElse(false);
		}
		
		// 3. 권한 계산
		boolean canEdit = false;
		boolean canDelete = false;
		if (currentMemberId != null) {
			// 작성자 본인인가?
			canEdit = post.getMember().getMemberId().equals(currentMemberId);
			// 작성자 본인이거나, 운영진인가?
			canDelete = canEdit || isManager;
		}
		
		// 4. DTO로 변환하여 리턴 (권한 정보 담기)
		return convertToDetailDTO(post, canEdit, canDelete);
	}
	
	// Entity -> DetailDTO 변환 메서드 (새로 만들기 또는 수정)
	private PostDetailResponseDTO convertToDetailDTO(PostEntity entity, boolean canEdit, boolean canDelete) {
		return PostDetailResponseDTO.builder()
				.postId(entity.getPostId())
				.title(entity.getTitle())
				.content(entity.getContent())
				.clubId(entity.getClub().getClubId())
				.memberId(entity.getMember().getMemberId())
				.imageUrl(entity.getImageUrl())
				.isNotice(entity.getIsNotice())
				// 날짜 포맷팅: T -> 공백
				.createdAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
				.canEdit(canEdit)
				.canDelete(canDelete)
				.build();
	}
	
	// 4. 게시글 삭제
	@Transactional
	public void deletePost(Integer postId, String currentMemberId) {
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
		
		// 운영진 여부 조회
		ClubMemberEntity clubMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(post.getClub().getClubId(), currentMemberId)
				.orElseThrow(() -> new IllegalArgumentException("해당 모임의 멤버가 아닙니다."));
		
		// 권한 체크: 작성자 본인 OR 운영진(OWNER/MANAGER)
		boolean isAuthor = post.getMember().getMemberId().equals(currentMemberId);
		boolean isManager = "OWNER".equals(clubMember.getRole()) || "MANAGER".equals(clubMember.getRole());
		
		if (isAuthor || isManager) {
			postRepository.delete(post);
		} else {
			throw new IllegalArgumentException("삭제 권한이 없습니다.");
		}
	}
	
	// 5. 게시글 수정
	@Transactional
	public void updatePost(Integer postId, PostDTO updatedPostDto, String currentMemberId) throws IOException {
		// 기존 게시글 엔티티를 DB에서 조회
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));
		
		// 수정 권한 체크: 작성자 본인만 가능
		if (!post.getMember().getMemberId().equals(currentMemberId)) {
			throw new IllegalArgumentException("수정 권한이 없습니다.");
		}
		
		// 수정 시에도 제목/내용 빈 값 체크
		validatePost(updatedPostDto);
		
		// 제목과 내용만 수정 가능하도록 설정
		post.setTitle(updatedPostDto.getTitle());
		post.setContent(updatedPostDto.getContent());
		
		// 새로운 이미지가 들어왔다면 교체
		if (updatedPostDto.getImage() != null && !updatedPostDto.getImage().isEmpty()) {
			String uuid = UUID.randomUUID().toString();
			String savedFileName = uuid + "_" + updatedPostDto.getImage().getOriginalFilename();
			
			// 폴더 생성 로직 추가
			File uploadDir = new File(getFullUploadPath());
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			
			File saveFile = new File(uploadDir, savedFileName);
			updatedPostDto.getImage().transferTo(saveFile);
			
			post.setImageUrl(savedFileName); // 새 이미지 경로로 업데이트
		}
		// Dirty Checking으로 자동 저장되므로 return post; 삭제
	}
	
	// 6. 게시글 검색
	public Page<PostDTO> getPostList(Integer clubId, String searchType, String searchKeyword, int page) {
		// 1. 정렬 조건 설정: 공지사항 우선(Desc) -> 최신글 순(PostId Desc)
		// Pageable 객체를 만들 때 정렬 조건을 한 번에 정의합니다.
		Sort sort = Sort.by(
				Sort.Order.desc("isNotice"),
				Sort.Order.desc("postId")
		);
		
		// 2. 페이징 설정: (요청 페이지 번호, 한 페이지당 개수, 정렬 정보)
		// 주의: JPA의 페이지는 0부터 시작합니다!
		Pageable pageable = PageRequest.of(page, 10, sort);
		Page<PostEntity> entities;
		
		// 3. 검색어 유무에 따른 분기 처리
		if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
			entities = postRepository.findByClub_ClubId(clubId, pageable);
		} else {
			switch (searchType) {
				case "title":
					entities = postRepository.findByClub_ClubIdAndTitleContaining(clubId, searchKeyword, pageable);
					break;
				case "content":
					entities = postRepository.findByClub_ClubIdAndContentContaining(clubId, searchKeyword, pageable);
					break;
				case "all":
				default:
					entities = postRepository.findByClub_ClubIdAndTitleContainingOrClub_ClubIdAndContentContaining(
							clubId, searchKeyword, clubId, searchKeyword, pageable);
					break;
			}
		}
		
		// 4. Page<Entity>를 Page<PostDTO>로 변환
		// Page 인터페이스가 제공하는 .map()을 사용하면 페이징 정보는 유지하면서 데이터만 DTO로 바꿀 수 있습니다.
		return entities.map(this::convertToDTO);
	}
	
	private PostDTO convertToDTO(PostEntity entity) {
		return PostDTO.builder()
				.postId(entity.getPostId())
				.title(entity.getTitle())
				.content(entity.getContent())
				.clubId(entity.getClub().getClubId())
				.memberId(entity.getMember().getMemberId())
				.imageUrl(entity.getImageUrl())
				.isNotice(entity.getIsNotice())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}
