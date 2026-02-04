package net.datasa.EnLink.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ClubScheduler {
	
	private final ClubRepository clubRepository;
	
	
//	@Scheduled(fixedDelay = 10000)      // 테스트용
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void deleteExpiredClubs() {
		// 일주일 전 시간 계산
		LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
		
		// 상태가 PENDING_DELETE이고, 삭제 요청일이 7일이 지난 모임들 조회
		List<ClubEntity> targetClubs = clubRepository.findByStatusAndDeletedAtBefore("DELETED_PENDING", oneWeekAgo);
		
		// 진짜 DB에서 삭제 (또는 최종 삭제 상태로 변경)
		clubRepository.deleteAll(targetClubs);
		
		System.out.println(targetClubs.size() + "개의 모임이 일주일이 지나 영구 삭제되었습니다.");
	}
}
