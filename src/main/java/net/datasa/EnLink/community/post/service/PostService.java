package net.datasa.EnLink.community.post.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.post.dto.PostDTO;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.post.repository.PostRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
	
	private final PostRepository postRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final ClubMemberRepository clubMemberRepository;
	
	// 이미지가 실제로 저장될 내 컴퓨터 폴더 경로 (미리 폴더 만들 것!!)
	private final String uploadPath = "C:/enlink_storage/";
	
	// 1. 게시글 저장하기 (PostDTO를 받도록 수정)
	@Transactional
	public void savePost(PostDTO postDTO) throws IOException {
		validatePost(postDTO);
		
		// [추가] ID로 실제 엔티티 객체를 찾아옵니다.
		ClubEntity club = clubRepository.findById(postDTO.getClubId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
		
		MemberEntity member = memberRepository.findById(postDTO.getMemberId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
		// 공지사항으로 등록하려고 하는 경우
		if (postDTO.getIsNotice() != null && postDTO.getIsNotice()) {
			// 이미 만들어두신 1번 메서드 활용!
			ClubMemberEntity clubMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(postDTO.getClubId(), postDTO.getMemberId())
					.orElseThrow(() -> new IllegalArgumentException("해당 모임의 멤버가 아닙니다."));
			
			// 가입 상태 확인 및 역할(Role) 확인
			boolean isManager = "OWNER".equals(clubMember.getRole()) || "MANAGER".equals(clubMember.getRole());
			boolean isActive = "ACTIVE".equals(clubMember.getStatus());
			
			if (!isActive || !isManager) {
				throw new IllegalArgumentException("공지사항은 모임 운영진(방장/매니저)만 작성할 수 있습니다.");
			}
		}
		
		
		String savedFileName = null;
		
		// 추가 이미지가 있다면 물리적으로 저장하는 로직
		if (postDTO.getImage() != null && !postDTO.getImage().isEmpty()) {
			MultipartFile imageFile = postDTO.getImage();
			
			// 중복 방지 이름 생성: uuid_원본이름.jpg
			String uuid = UUID.randomUUID().toString();
			savedFileName = uuid + "_" + imageFile.getOriginalFilename();
			
			// 파일 저장 실행
			File saveFile = new File(uploadPath, savedFileName);
			imageFile.transferTo(saveFile);
		}
		
		// DTO의 데이터를 Entity로 옮겨담기 (Builder 방식)
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
	
	// [추가] 공통 검증 메서드 (중복 코드를 줄이기 위해 별도로 뺍니다)
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
	
	// 3. 게시글 상세 내용 하나 가져오기
	public PostDTO getPostById(Integer postId) {
		// 1. DB에서 엔티티를 찾습니다.
		PostEntity entity = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));
		
		// 2. 엔티티의 데이터를 DTO로 옮깁니다. (빌더 패턴 사용)
		return PostDTO.builder()
				.postId(entity.getPostId())
				.clubId(entity.getClub().getClubId())       // 객체 참조에서 ID 추출
				.memberId(entity.getMember().getMemberId()) // 객체 참조에서 ID 추출
				.title(entity.getTitle())
				.content(entity.getContent())
				.imageUrl(entity.getImageUrl())
				.createdAt(entity.getCreatedAt())
				.build();
	}
	
	// 4. 게시글 삭제
	@Transactional
	public void deletePost(Integer postId) {
		postRepository.deleteById(postId);
	}
	
	// 5. 게시글 수정
	@Transactional
	public void updatePost(Integer postId, PostDTO updatedPostDto) throws IOException {
		// 기존 게시글 엔티티를 DB에서 조회
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));
		
		// [수정] 수정 시에도 제목/내용 빈 값 체크
		validatePost(updatedPostDto);
		
		// 제목과 내용만 수정 가능하도록 설정
		post.setTitle(updatedPostDto.getTitle());
		post.setContent(updatedPostDto.getContent());
		
		// [추가] 새로운 이미지가 들어왔다면 교체
		if (updatedPostDto.getImage() != null && !updatedPostDto.getImage().isEmpty()) {
			String uuid = UUID.randomUUID().toString();
			String savedFileName = uuid + "_" + updatedPostDto.getImage().getOriginalFilename();
			
			File saveFile = new File(uploadPath, savedFileName);
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
