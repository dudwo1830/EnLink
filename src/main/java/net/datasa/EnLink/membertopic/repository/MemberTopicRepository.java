package net.datasa.EnLink.membertopic.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.membertopic.entity.MemberTopicEntity;

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

}
