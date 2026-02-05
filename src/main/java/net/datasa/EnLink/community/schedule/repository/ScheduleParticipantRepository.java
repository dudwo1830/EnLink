package net.datasa.EnLink.community.schedule.repository;

import net.datasa.EnLink.community.schedule.entity.ScheduleEntity;
import net.datasa.EnLink.community.schedule.entity.ScheduleParticipantEntity;
import net.datasa.EnLink.community.schedule.entity.ScheduleParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipantEntity, ScheduleParticipantId> {
	// 특정 일정(schedId)에 특정 상태(status)로 참여 중인 모든 엔티티 리스트 조회
	List<ScheduleParticipantEntity> findBySchedule_SchedIdAndStatus(Integer schedId, String status);
	
	// 특정 일정에 참여 중인 인원수 계산 (status가 JOIN인 경우)
	long countBySchedule_SchedIdAndStatus(Integer schedId, String status);
	
	// 특정 회원이 특정 일정에 이미 참여 신청을 했는지 확인
	Optional<ScheduleParticipantEntity> findByScheduleAndMember_MemberId(ScheduleEntity schedule, String memberId);
}
