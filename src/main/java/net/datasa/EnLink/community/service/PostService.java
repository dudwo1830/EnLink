package net.datasa.EnLink.community.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.PostDTO;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
	
	private final PostRepository postRepository;
	
	// 이미지가 실제로 저장될 내 컴퓨터 폴더 경로 (미리 폴더 만들 것!!)
	private final String uploadPath = "C:/enlink_uploads/";
	
	// 1. 게시글 저장하기 (PostDTO를 받도록 수정)
	@Transactional
	public PostEntity savePost(PostDTO postDTO) throws IOException {
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
				.clubId(postDTO.getClubId())
				.memberId(postDTO.getMemberId())
				.title(postDTO.getTitle())
				.content(postDTO.getContent())
				.imageUrl(savedFileName)
				.createdAt(LocalDateTime.now())
				.build();
		
		return postRepository.save(postEntity);
	}
	
	// 2. 특정 모임의 모든 게시글 가져오기
	public List<PostEntity> getPostsByClub(Integer clubId) {
		return postRepository.findByClubId(clubId);
	}
	
	// 3. 게시글 상세 내용 하나 가져오기
	public PostEntity getPostById(Integer postId) {
		return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));
	}
	
	// 4. 게시글 삭제
	@Transactional
	public void deletePost(Integer postId) {
		postRepository.deleteById(postId);
	}
	
	// 5. 게시글 수정
	@Transactional
	public PostEntity updatePost(Integer postId, PostDTO updatedPostDto) throws IOException {
		PostEntity post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));
		
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
		
		return post;	// @Transactional에 의해 자동 저장(Dirty Checking)
	}
}
