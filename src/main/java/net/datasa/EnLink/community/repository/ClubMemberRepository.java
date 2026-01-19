package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMemberEntity, Integer> {
	
	// 특정 모임의 모든 멤버 목록 조회
	List<ClubMemberEntity> findByClubId(Integer clubId);
	
	// 특정 사용자가 가입한 모임 정보 조회 (마이페이지용)
	List<ClubMemberEntity> findByMemberId(String memberId);
	
	// 특정 모임에 특정 유저가 있는지 확인 (중복 가입 방지 체크용)
	Optional<ClubMemberEntity> findByClubIdAndMemberId(Integer clubId, String memberId);
	
	// 특정 모임의 권한별 멤버 수 조회 (예: 운영진 수 체크 등)
	long countByClubIdAndRole(Integer clubId, String role);
}
