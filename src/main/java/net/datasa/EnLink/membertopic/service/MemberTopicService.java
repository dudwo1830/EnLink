package net.datasa.EnLink.membertopic.service;

import java.util.List;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.membertopic.repository.MemberTopicRepository;
import net.datasa.EnLink.topic.dto.response.TopicWithCheckResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberTopicService {
	private final MemberTopicRepository memberTopicRepository;

	public List<TopicWithCheckResponse> getCheckListAll(String memberId) {
		return memberTopicRepository.findAllWithCheck(memberId);
	}
}
