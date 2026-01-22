package net.datasa.EnLink.community.post.repository;

import net.datasa.EnLink.community.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
	
	// 특정 모임(clubId)에 속한 게시글만 가져오는 기능 추가
	List<PostEntity> findByClubId(Integer clubId);
	
	// 작성자(memberId)가 쓴 글들만 가져오는 기능 추가
	List<PostEntity> findByMemberId(String memberId);
}
