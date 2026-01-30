package net.datasa.EnLink.community.gallery.repository;

import net.datasa.EnLink.community.gallery.entity.GalleryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalleryRepository extends JpaRepository<GalleryEntity, Integer> {
	
	/**
	 * 특정 모임(clubId)의 사진 목록을 최신순으로 페이징 조회
	 * @param clubId 조회할 모임 식별자
	 * @param pageable 페이징 및 정렬 정보
	 * @return 사진 엔티티 페이지
	 */
	Page<GalleryEntity> findByClub_ClubId(Integer clubId, Pageable pageable);
	
	// [추가 가능] 만약 공개 여부가 'Y'인 것만 보여줘야 한다면 (WBS 설정 기능 대비)
	//	 Page<GalleryEntity> findByClub_ClubIdAndIsPublic(Integer clubId, String isPublic, Pageable pageable);
}
