package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubJoinAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
	public interface ClubAnswerRepository extends JpaRepository<ClubJoinAnswerEntity, Integer> {
	
	/**
	 * 특정 모임에 대한 특정 유저의 가입 신청 답변을 조회합니다.
	 * 가입 승인 대기 상태에서 답변 내용을 확인할 때 사용합니다.
	 */
	Optional<ClubJoinAnswerEntity> findByClubIdAndMemberId(Integer clubId, String memberId);
	
	/**
	 * 신청 취소 또는 거절 시, 해당 유저가 작성했던 답변 데이터를 즉시 삭제합니다.
	 */
	@Modifying
	@Query("DELETE FROM ClubJoinAnswerEntity a WHERE a.clubId = :clubId AND a.memberId = :memberId")
	void deleteByClubIdAndMemberId(@Param("clubId") Integer clubId, @Param("memberId") String memberId);
	
	/**
	 * 모임 영구 삭제 시, 해당 모임과 관련된 모든 답변 데이터를 일괄 삭제합니다.
	 */
	void deleteByClubId(Integer clubId);
}

