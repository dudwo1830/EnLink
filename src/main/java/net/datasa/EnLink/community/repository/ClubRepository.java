package net.datasa.EnLink.community.repository;

import net.datasa.EnLink.community.dto.ClubSummaryResponse;
import net.datasa.EnLink.community.entity.ClubEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {

	/**
	 * 모임 이름의 중복 여부를 확인합니다.
	 */
	boolean existsByName(String name);
	
	boolean existsByNameAndClubIdNot(String name, Integer clubId);
	
	/**
	 * 삭제 유예 기간(7일)이 경과한 '삭제 대기' 상태의 모임들을 조회합니다.
	 * (Batch 작업이나 스케줄러를 통한 영구 삭제 시 활용)
	 */
	List<ClubEntity> findByStatusAndDeletedAtBefore(String status, LocalDateTime dateTime);

	/**
	 * 특정 상태(ACTIVE, DELETED_PENDING 등)의 모임 목록을 조회합니다.
	 */
	List<ClubEntity> findByStatus(String status);
	
	
	// [비로그인용] 전체에서 랜덤하게 N개 추출
	@Query(value = "SELECT * FROM club WHERE status = 'ACTIVE' AND locale = :locale ORDER BY RAND() LIMIT :limit", nativeQuery = true)
	List<ClubEntity> findRandomActiveClubs(@Param("limit") int limit, @Param("locale") String locale);
	
	// [로그인용] 1~4순위 통합 추천 로직
	@Query(value = """
        SELECT *,
               (CASE
                   WHEN city_id = :cityId AND topic_id = :topicId THEN 100
                   WHEN city_id = :cityId THEN 80
                   WHEN topic_id = :topicId THEN 50
                   ELSE 0
                END) AS match_score
        FROM clubs
        WHERE status = 'ACTIVE'
        AND locale = :locale
        ORDER BY match_score DESC, RAND()
        LIMIT 20
        """, nativeQuery = true)
	List<ClubEntity> findRecommendedClubs(@Param("cityId") Long cityId,
										  @Param("topicId") Long topicId,
										  @Param("locale") String locale);
	
	/**
	 * 특정 상태와 주제를 포함한 모임 목록을 조회합니다.
	 */
	List<ClubEntity> findByStatusAndTopic_TopicId(String status, Integer topicId);

	// 모임 리스트 조회 및 페이징 처리, 검색
	@Query("""
			select new net.datasa.EnLink.community.dto.ClubSummaryResponse(
				c.clubId,
				c.name,
				case 
					when :locale = 'ja' then t.nameJa
					else t.nameKo
				end,
				r.nameLocal,
				ci.nameLocal,
				c.imageUrl,
				c.description,
				count(cm),
				c.maxMember
			)
			from ClubEntity c
			join c.city ci
			join ci.region r
			join c.topic t
			left join ClubMemberEntity cm
				on cm.club = c and cm.status = 'ACTIVE'
			where
				(c.locale = :locale)
			and	(:cityId is null or ci.cityId = :cityId)
			and (:regionId is null or r.regionId = :regionId)
			and (:topicId is null or t.topicId = :topicId)
			and (
				:search is null
				or c.name like concat('%', :search, '%')
				or c.description like concat('%', :search, '%')
			)
			group by
				c.clubId,
				c.name,
				case 
					when :locale = 'ja' then t.nameJa
					else t.nameKo
				end,
				r.nameLocal,
				ci.nameLocal,
				c.imageUrl,
				c.description,
				c.maxMember
			order by c.clubId desc
			""")
	Slice<ClubSummaryResponse> searchClubs(Pageable pageable,
			@Param("cityId") Integer cityId,
			@Param("topicId") Integer topicId,
			@Param("search") String search,
			@Param("regionId") Integer regionId,
			@Param("locale") String locale);
	
@Query("""
    select new net.datasa.EnLink.community.dto.ClubSummaryResponse(
        c.clubId, c.name,
        case when :locale = 'ja' then t.nameJa else t.nameKo end,
        r.nameLocal, ci.nameLocal,
        c.imageUrl, c.description,
        count(cm), c.maxMember
    )
    from ClubEntity c
    join c.topic t
    join c.city ci
    join ci.region r
    left join ClubMemberEntity cm on cm.club = c and cm.status = 'ACTIVE'
    where c.status = 'ACTIVE'
      and c.locale = :locale
      and (:topicId is null or t.topicId = :topicId)
      and (:cityId is null or ci.cityId = :cityId)
      and (:regionId is null or r.regionId = :regionId)
      and (:search is null or c.name like concat('%', :search, '%') or c.description like concat('%', :search, '%'))
    group by 
        c.clubId, c.name, t.nameJa, t.nameKo, r.nameLocal, ci.nameLocal, c.imageUrl, c.description, c.maxMember
    order by c.clubId desc
    """)
List<ClubSummaryResponse> findClubSummary(
	@Param("topicId") Integer topicId,
	@Param("regionId") Integer regionId,
	@Param("cityId") Integer cityId,
	@Param("search") String search,
	@Param("locale") String locale);
}

