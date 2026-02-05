package net.datasa.EnLink.community.schedule.repository;

import net.datasa.EnLink.community.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {
	// 특정 모임의 모든 일정을 일시 순으로 가져오기
	List<ScheduleEntity> findByClub_ClubIdOrderByEventDateAsc(Integer clubId);
}
