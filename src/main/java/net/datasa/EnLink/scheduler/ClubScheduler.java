package net.datasa.EnLink.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.community.service.ClubManageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ClubScheduler {
	
	private final ClubRepository clubRepository;
	private final ClubManageService clubManageService;
	
	// 매일 자정 실행
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void deleteExpiredClubs() {
		// 1. 7일 전 시간 계산
		LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
		
		// 2. 삭제 대기 중(DELETED_PENDING)이면서 7일이 지난 모임 조회
		List<ClubEntity> targetClubs = clubRepository.findByStatusAndDeletedAtBefore("DELETED_PENDING", oneWeekAgo);
		
		if (!targetClubs.isEmpty()) {
			
			for (ClubEntity club : targetClubs) {
				clubManageService.deleteRealFile(club.getImageUrl());
			}
			
			// 4. DB에서 영구 삭제
			clubRepository.deleteAll(targetClubs);
			
			System.out.println(targetClubs.size() + "개의 모임과 관련 이미지 파일이 영구 삭제되었습니다.");
		}
	}
}
