package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberHistoryRepository extends JpaRepository<ClubMemberHistoryEntity, Integer> {
	
	/**
	 * 특정 멤버의 가장 최근 활동 기록을 조회합니다.
	 * 주로 재가입 제한 기간(쿨타임) 확인이나 마지막 탈퇴 사유를 파악할 때 사용합니다.
	 */
	Optional<ClubMemberHistoryEntity> findFirstByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(Integer clubId, String memberId);
	
	/**
	 * 특정 멤버의 전체 활동 이력을 최신순으로 조회합니다.
	 * 멤버 상세 페이지의 '활동 타임라인'을 구성할 때 활용합니다.
	 */
	List<ClubMemberHistoryEntity> findByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(Integer clubId, String memberId);
	
	/**
	 * 모임 삭제 시 해당 모임과 관련된 모든 히스토리 데이터를 일괄 삭제합니다.
	 */
	void deleteByClub_ClubId(Integer clubId);
}