package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
	
	// 모임 이름 중복 체크를 위해 유용하게 쓰일 메서드
	boolean existsByName(String name);
	
	// 모임 이름을 통해 정보를 찾을 때 사용
	Optional<ClubEntity> findByName(String name);
	
	List<ClubEntity> findByStatusAndDeletedAtBefore(String status, LocalDateTime dateTime);
	
	// 상태가 'ACTIVE'인 모임들만 리스트로 가져오는 메서드 추가
	List<ClubEntity> findByStatus(String status);
}

