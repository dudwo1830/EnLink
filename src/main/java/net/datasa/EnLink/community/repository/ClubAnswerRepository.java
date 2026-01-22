package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubJoinAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
	public interface ClubAnswerRepository extends JpaRepository<ClubJoinAnswerEntity, Integer> {
		
		/**
		 * 특정 모임에서 특정 유저가 작성한 가입 답변 조회
		 * @param clubId 모임 고유번호
		 * @param memberId 회원 ID
		 * @return 가입 답변 엔티티
		 */
		Optional<ClubJoinAnswerEntity> findByClubIdAndMemberId(Integer clubId, String memberId);
		
		/**
		 * 신청 취소 시 답변 데이터 삭제를 위한 메서드
		 * @param clubId 모임 고유번호
		 * @param memberId 회원 ID
		 */
		void deleteByClubIdAndMemberId(Integer clubId, String memberId);
	}

