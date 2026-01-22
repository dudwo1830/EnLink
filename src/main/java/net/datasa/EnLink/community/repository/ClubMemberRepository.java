package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMemberEntity, Integer> {
	
	// 1. 상세 페이지 버튼 제어용: 특정 유저가 이 모임에 어떤 상태(PENDING, ACTIVE 등)로 있는지 조회
	Optional<ClubMemberEntity> findByClub_ClubIdAndMember_MemberId(Integer clubId, String memberId);
	
	// 2. 중복 신청 방지용: 이미 데이터가 존재하는지 boolean으로 빠르게 확인
	boolean existsByClub_ClubIdAndMember_MemberId(Integer clubId, String memberId);
	
	// 3. 모임 관리(승인 현황)용: 특정 모임의 모든 멤버/신청자 목록 조회
	List<ClubMemberEntity> findByClub_ClubId(Integer clubId);
	
	// 4. 모임 관리(승인 현황)용 필터링: 특정 모임에서 특정 상태(예: 'PENDING')인 사람들만 조회
	List<ClubMemberEntity> findByClub_ClubIdAndStatus(Integer clubId, String status);
	
	// 5. 마이페이지용: 내가 가입한(또는 신청한) 모든 모임 목록 조회
	List<ClubMemberEntity> findByMember_MemberId(String memberId);
	
	// 6. 정원 체크용: 현재 이 모임에 정회원(ACTIVE)이 몇 명인지 카운트
	long countByClub_ClubIdAndStatus(Integer clubId, String status);
}
