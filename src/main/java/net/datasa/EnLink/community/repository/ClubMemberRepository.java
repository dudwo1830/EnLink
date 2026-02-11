package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMemberEntity, Integer> {
	
	/**
	 * 특정 모임과 멤버의 관계 정보를 조회합니다. (권한 및 가입 상태 확인용)
	 */
	Optional<ClubMemberEntity> findByClub_ClubIdAndMember_MemberId(Integer clubId, String memberId);
	
	/**
	 * 특정 모임과 멤버의 가장 최근 가입 이력을 조회합니다.
	 */
	Optional<ClubMemberEntity> findFirstByClub_ClubIdAndMember_MemberIdOrderByJoinedAtDesc(Integer clubId, String memberId);
	
	/**
	 * 유저가 특정 상태(ACTIVE, PENDING 등)로 가입된 모든 모임 목록을 조회합니다.
	 */
	List<ClubMemberEntity> findByMember_MemberIdAndStatus(String memberId, String status);
	
	/**
	 * 특정 모임의 멤버 목록을 권한 순으로 정렬하여 조회합니다. (운영진 -> 일반멤버)
	 */
	List<ClubMemberEntity> findByClub_ClubIdAndStatusOrderByRoleAsc(Integer clubId, String status);
	
	/**
	 * [5+5 쿼터제] 유저가 '방장(OWNER)'으로서 점유 중인 쿼터를 계산합니다.
	 * 삭제 유예(DELETED_PENDING) 상태인 모임도 복구 가능성을 고려하여 개수에 포함합니다.
	 */
	@Query("SELECT COUNT(cm) FROM ClubMemberEntity cm " +
			"WHERE cm.member.memberId = :memberId " +
			"AND cm.role = 'OWNER' " +
			"AND cm.status = 'ACTIVE' " +
			"AND cm.club.status IN ('ACTIVE', 'DELETED_PENDING')")
	long countOwnerQuota(@Param("memberId") String memberId);
	
	/**
	 * [모임 복구 전용] 나(currentClubId)를 제외하고 방장으로서 점유 중인 쿼터를 계산합니다.
	 */
	@Query("SELECT COUNT(cm) FROM ClubMemberEntity cm " +
			"WHERE cm.member.memberId = :memberId " +
			"AND cm.role = 'OWNER' " +
			"AND cm.status = 'ACTIVE' " +
			"AND cm.club.clubId <> :currentClubId " +
			"AND cm.club.status IN ('ACTIVE', 'DELETED_PENDING')")
	long countOwnerQuotaExcludingCurrent(@Param("memberId") String memberId, @Param("currentClubId") Integer currentClubId);
	
	/**
	 * [5+5 쿼터제] 유저가 '참여 멤버(MANAGER, MEMBER)'로서 점유 중인 쿼터를 계산합니다.
	 * 삭제 유예 상태인 모임도 포함합니다.
	 */
	@Query("SELECT COUNT(cm) FROM ClubMemberEntity cm " +
			"WHERE cm.member.memberId = :memberId " +
			"AND cm.role IN ('MANAGER', 'MEMBER') " +
			"AND cm.status = 'ACTIVE' " +
			"AND cm.club.status IN ('ACTIVE', 'DELETED_PENDING')")
	long countParticipantQuota(@Param("memberId") String memberId);
	
	/**
	 * 특정 모임의 현재 활동 멤버(ACTIVE) 인원수를 확인합니다. (정원 체크용)
	 */
	int countByClub_ClubIdAndStatus(Integer clubId, String status);
	
	/**
	 * 특정 모임의 멤버 가입 현황을 페이징하여 조회합니다. (관리자 페이지용)
	 */
	Page<ClubMemberEntity> findByClub_ClubIdAndStatus(Integer clubId, String status, Pageable pageable);
	
	/**
	 * 모임 영구 삭제 시 관련 멤버 데이터를 일괄 제거합니다.
	 */
	void deleteByClub_ClubId(Integer clubId);
}

