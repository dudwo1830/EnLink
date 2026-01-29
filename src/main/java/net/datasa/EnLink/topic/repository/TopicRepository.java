package net.datasa.EnLink.topic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.topic.dto.response.TopicWithCheckResponse;
import net.datasa.EnLink.topic.entity.TopicEntity;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Integer> {

	@Query("""
			select new net.datasa.EnLink.topic.dto.response.TopicWithCheckResponse(
					t.topicId,
					t.name,
					case
						when mti.member.memberId is not null then true
						else false
					end as checked
				)
			from TopicEntity t
			left join MemberTopicEntity mti
			on mti.topic = t
			and mti.member.memberId = :memberId
			""")
	List<TopicWithCheckResponse> findAllWithCheck(@Param("memberId") String memberId);

}
