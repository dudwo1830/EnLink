package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
	
	/**
	 * 모임 이름의 중복 여부를 확인합니다.
	 */
	boolean existsByName(String name);
	
	boolean existsByNameAndClubIdNot(String name, Integer clubId);
	
	/**
	 * 삭제 유예 기간(7일)이 경과한 '삭제 대기' 상태의 모임들을 조회합니다.
	 * (Batch 작업이나 스케줄러를 통한 영구 삭제 시 활용)
	 */
	List<ClubEntity> findByStatusAndDeletedAtBefore(String status, LocalDateTime dateTime);
	
	/**
	 * 특정 상태(ACTIVE, DELETED_PENDING 등)의 모임 목록을 조회합니다.
	 */
	List<ClubEntity> findByStatus(String status);

	/**
	 * 특정 상태와 주제를 포함한 모임 목록을 조회합니다.
	 */
	List<ClubEntity> findByStatusAndTopic_TopicId(String status, Integer topicId);

	// 모임 리스트 조회 및 페이징 처리, 검색
	@Query("""
			select cb from ClubEntity cb
			join cb.city c
			join cb.topic t
			where
				(:cityId is null
					or c.cityId = :cityId)
			and
				(:regionId is null
					or c.region.regionId = :regionId)
			and
				(:topicId is null
					or t.topicId = :topicId)
			and
				(:search is null
					or cb.name like %:search%
					or cb.description like %:search%)
			and
				 (cb.status like "ACTIVE")
			""")
	Slice<ClubEntity> searchClubs(Pageable pageable,
			@Param("cityId") Integer cityId,
			@Param("topicId") Integer topicId,
			@Param("search") String search,
			@Param("regionId") Integer regionId);
}
