package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.entity.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
	
	// 모임 이름 중복 체크를 위해 유용하게 쓰일 메서드
	boolean existsByName(String name);
	
	// 모임 이름을 통해 정보를 찾을 때 사용
	Optional<ClubEntity> findByName(String name);
}
