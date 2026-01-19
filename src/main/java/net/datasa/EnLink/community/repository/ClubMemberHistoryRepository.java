package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubMemberHistoryRepository extends JpaRepository<ClubMemberHistoryEntity, Integer> {
	
	// 특정 모임에서 발생한 모든 이력 최신순 조회
	List<ClubMemberHistoryEntity> findByClubIdOrderByCreatedAtDesc(Integer clubId);
	
	// 특정 유저(대상자)에 대한 이력 조회
	List<ClubMemberHistoryEntity> findByTargetMemberId(String targetMemberId);
}
