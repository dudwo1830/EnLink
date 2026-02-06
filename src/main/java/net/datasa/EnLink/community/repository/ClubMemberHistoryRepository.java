package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberHistoryRepository extends JpaRepository<ClubMemberHistoryEntity, Integer> {
	
	// 특정 유저의 가장 최근 활동 이력 하나를 가져옴 (쿨타임 계산용)
	// EXIT(탈퇴)나 JOIN_REJECT(거절) 기록을 찾을 때 사용
	Optional<ClubMemberHistoryEntity> findFirstByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(Integer clubId, String memberId);
	
	// 특정 유저의 모든 활동 이력을 최신순으로 리스트업 ✅
	List<ClubMemberHistoryEntity> findByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(Integer clubId, String memberId);
}