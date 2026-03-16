package net.datasa.EnLink.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.community.service.ClubManageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubScheduler {
	
	private final ClubRepository clubRepository;
	private final ClubManageService clubManageService;
	
	// 매일 자정 실행
	@Scheduled(cron = "0 0 0 * * *")
	public void deleteExpiredClubs() {
		// 1. 7일 전 시간 계산
		LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
		
		// 2. 삭제 대기 중(DELETED_PENDING)이면서 7일이 지난 모임 조회
		List<ClubEntity> targetClubs = clubRepository.findByStatusAndDeletedAtBefore("DELETED_PENDING", oneWeekAgo);
		
		for (ClubEntity club : targetClubs) {
			try {
				clubManageService.hardDeleteClubByScheduler(club.getClubId());
			} catch (Exception e) {
				log.error("삭제 실패: {}", e.getMessage());
			}
		}
		
		log.info("{}개의 만료된 모임이 시스템에 의해 영구 삭제되었습니다.", targetClubs.size());
	}
}
