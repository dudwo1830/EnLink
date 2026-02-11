package net.datasa.EnLink.community.chat.repository;

import net.datasa.EnLink.community.chat.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Integer> {
	// 특정 모임(clubId)의 메시지를 최신순(SentAt 내림차순)으로 페이징 조회
	// '무한 스크롤' 기능을 위해, 데이터를 한꺼번에 다 가져오지 않고 페이지 단위로 끊어 가져오는 Slice를 사용
	Slice<ChatMessageEntity> findByClub_ClubIdOrderBySentAtDesc(Integer clubId, Pageable pageable);
	
}
