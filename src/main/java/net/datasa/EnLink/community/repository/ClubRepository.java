package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
	
	/**
	 * 모임 이름의 중복 여부를 확인합니다.
	 */
	boolean existsByName(String name);
	
	/**
	 * 삭제 유예 기간(7일)이 경과한 '삭제 대기' 상태의 모임들을 조회합니다.
	 * (Batch 작업이나 스케줄러를 통한 영구 삭제 시 활용)
	 */
	List<ClubEntity> findByStatusAndDeletedAtBefore(String status, LocalDateTime dateTime);
	
	/**
	 * 특정 상태(ACTIVE, DELETED_PENDING 등)의 모임 목록을 조회합니다.
	 */
	List<ClubEntity> findByStatus(String status);
}

