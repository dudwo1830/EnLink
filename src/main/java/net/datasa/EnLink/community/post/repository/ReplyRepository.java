package net.datasa.EnLink.community.post.repository;

import net.datasa.EnLink.community.post.entity.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<ReplyEntity, Integer> {
	// 특정 게시글의 모든 댓글을 가져오는 메서드 (ID순 정렬)
	List<ReplyEntity> findByPost_PostIdOrderByReplyIdAsc(Integer postId);
}
