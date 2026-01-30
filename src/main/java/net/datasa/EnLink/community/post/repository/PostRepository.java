package net.datasa.EnLink.community.post.repository;

import net.datasa.EnLink.community.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
	
	// 1. 기본 목록 (페이징 적용)
	Page<PostEntity> findByClub_ClubId(Integer clubId, Pageable pageable);
	
	// 2. 제목 + 내용 검색 (페이징 적용)
	Page<PostEntity> findByClub_ClubIdAndTitleContainingOrClub_ClubIdAndContentContaining(
			Integer clubId1, String title, Integer clubId2, String content, Pageable pageable);
	
	// 3. 제목만 검색 (페이징 적용)
	Page<PostEntity> findByClub_ClubIdAndTitleContaining(Integer clubId, String title, Pageable pageable);
	
	// 4. 내용만 검색 (페이징 적용)
	Page<PostEntity> findByClub_ClubIdAndContentContaining(Integer clubId, String content, Pageable pageable);
}
