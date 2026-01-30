package net.datasa.EnLink.membertopic.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import net.datasa.EnLink.membertopic.entity.MemberTopicEntity;
import net.datasa.EnLink.membertopic.repository.MemberTopicRepository;
import net.datasa.EnLink.topic.entity.TopicEntity;
import net.datasa.EnLink.topic.repository.TopicRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberTopicService {
	private final MemberTopicRepository memberTopicRepository;
	private final MemberRepository memberRepository;
	private final TopicRepository topicRepository;

	public void replaceTopics(String memberId, List<Integer> newTopicIds) {
		// 회원 정보
		MemberEntity memberEntity = memberRepository.findById(memberId).orElse(null);
		// 변경할 주제 리스트
		List<TopicEntity> newTopics = topicRepository.findAllById(newTopicIds);

		// 기존 회원_주제 데이터에서 주제 id만 가져옴
		Set<Integer> currentTopicIdSet = memberTopicRepository.findTopicIdsByMember(memberEntity);
		// List -> Set 변환
		Set<Integer> newTopicIdSet = new HashSet<>(newTopicIds);

		/* 필요한 부분만 변경하도록 계산하는 작업 */

		// 삭제할 주제 id
		Set<Integer> deleteSet = new HashSet<>(currentTopicIdSet);
		// 기존 데이터에서 새로운 데이터의 요소와 중복되는 부분을 제거 = 제거할 id만 남음
		deleteSet.removeAll(newTopicIdSet);

		if (!deleteSet.isEmpty()) {
			// 삭제 처리
			memberTopicRepository.deleteByMemberAndTopicIds(memberEntity, deleteSet);
		}

		// 추가할 주제 id
		Set<Integer> addSet = new HashSet<>(newTopicIdSet);
		// 새로운 데이터에서 기존의 데이터와 중복되는 부분을 제거 = 추가할 id만 남음
		addSet.removeAll(currentTopicIdSet);

		// 엔티티 리스트 생성
		List<MemberTopicEntity> entities = newTopics.stream()
				.filter(topic -> {
					// 새로운 주제 리스트에서 추가될 id와 일치한것을 추출
					return addSet.contains(topic.getTopicId());
				}).map(topic -> {
					// 해당 주제를 기반으로 엔티티 생성 및 반환
					return MemberTopicEntity.builder()
							.member(memberEntity)
							.topic(topic)
							.build();
				}).toList();

		memberTopicRepository.saveAll(entities);
	}
}
