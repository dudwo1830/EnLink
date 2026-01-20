package net.datasa.EnLink.community.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.post.entity.PostEntity;
import net.datasa.EnLink.community.post.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
	
	private final PostRepository postRepository;
	
	// 1. 글 저장하기
	@Transactional
	public PostEntity savePost(PostEntity postEntity) {
		return postRepository.save(postEntity);
	}
	
	// 2. 특정 모임의 모든 글 가져오기
	public List<PostEntity> getPostsByClub(Integer clubId) {
		return postRepository.findByClubId(clubId);
	}
	
	// 3. 글 상세 내용 하나 가져오기
	public PostEntity getPostById(Integer postId) {
		return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));
	}
	
	// 4. 글 삭제
	@Transactional
	public void deletePost(Integer postId) {
		postRepository.deleteById(postId);
	}
}
