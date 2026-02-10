package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMemberEntity, Integer> {
	// 페이징을 지원하는 조회 메서드 (Pageable 파라미터가 핵심!)
	Page<ClubMemberEntity> findByMember_MemberIdAndStatus(String memberId, String status, Pageable pageable);
	
	List<ClubMemberEntity> findByMember_MemberIdAndStatus(String memberId, String status);
	/**
	 * [조회 및 검증]
	 */
	// 특정 유저가 특정 모임에 속해있는지 상세 정보 조회 (상태 확인용)
	Optional<ClubMemberEntity> findByClub_ClubIdAndMember_MemberId(Integer clubId, String memberId);
	
	// 중복 가입 신청 여부 확인
	boolean existsByClub_ClubIdAndMember_MemberId(Integer clubId, String memberId);
	
	// 가입신청현황 페이징용
	Page<ClubMemberEntity> findByClub_ClubIdAndStatus(Integer clubId, String status, Pageable pageable);
	
	/**
	 * [모임 관리자용 (Manage)]
	 */
	// 특정 모임의 멤버 목록 조회 (상태별 필터링 + 권한 정렬)
	// 예: 승인 대기자(PENDING) 조회 또는 활동 멤버(ACTIVE) 조회
	List<ClubMemberEntity> findByClub_ClubIdAndStatusOrderByRoleAsc(Integer clubId, String status);
	
	/**
	 * [사용자 마이페이지용 (Member Activity)]
	 */
	// 1. 유저의 모든 활동 내역 조회 (전체)
	List<ClubMemberEntity> findByMember_MemberId(String memberId);
	
	/**
	 * [집계 및 카운트]
	 */
	// 특정 모임의 현재 활동 인원 수 (정원 체크용)
	int countByClub_ClubIdAndStatus(Integer clubId, String status);
	
	// 특정 역할(OWNER)의 개수 카운트
	long countByMember_MemberIdAndRole(String memberId, String role);
	
	// 현재 활동중이 모임 수 조회
	long countByMember_MemberIdAndStatus(String memberId, String status);
	
	// 🚨 [필수] 여러 이력 중 가장 최근의 상태 하나만 가져오기
	Optional<ClubMemberEntity> findFirstByClub_ClubIdAndMember_MemberIdAndStatusInOrderByJoinedAtDesc(
			Integer clubId,
			String memberId,
			List<String> statuses // 여기에 List.of("EXIT", "BANNED")를 넣을 거예요.
	);
	
	// 1️⃣ [상세 페이지용] 상태 상관없이 가장 최신 기록 1건 (현재 내 상태 확인용)
	Optional<ClubMemberEntity> findFirstByClub_ClubIdAndMember_MemberIdOrderByJoinedAtDesc(
			Integer clubId, String memberId
	);
	
	// 전체 가입이력 조회
	List<ClubMemberEntity> findByClub_ClubIdAndMember_MemberIdOrderByJoinedAtDesc(
			Integer clubId, String memberId
	);
	
	// 1. [추가] 모임 복구 시 소유 개수 체크를 위한 쿼리
	long countByMember_MemberIdAndRoleAndStatus(String memberId, String role, String status);
}

