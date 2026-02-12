package net.datasa.EnLink.membertopic.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.membertopic.entity.MemberTopicEntity;
import net.datasa.EnLink.topic.dto.response.TopicWithCheckResponse;

@Repository
public interface MemberTopicRepository extends JpaRepository<MemberTopicEntity, Integer> {

	@Query("""
			select mti.topic.topicId
			from MemberTopicEntity mti
			where mti.member = :member
			""")
	Set<Integer> findTopicIdsByMember(
			@Param("member") MemberEntity memberEntity);

	@Modifying
	@Query("""
				delete from MemberTopicEntity mti
				where mti.member = :member
				and mti.topic.id in :topicIds
			""")
	void deleteByMemberAndTopicIds(
			@Param("member") MemberEntity memberEntity,
			@Param("topicIds") Set<Integer> deleteSet);

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
