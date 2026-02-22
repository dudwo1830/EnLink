package net.datasa.EnLink.topic.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.dto.request.TopicCreateRequest;
import net.datasa.EnLink.topic.dto.request.TopicUpdateRequest;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;
import net.datasa.EnLink.topic.entity.TopicEntity;
import net.datasa.EnLink.topic.repository.TopicRepository;

@Transactional
@Service
@RequiredArgsConstructor
public class TopicService {
	private final TopicRepository topicRepository;

	@CacheEvict(value = "topics", allEntries = true)
	@PreAuthorize("hasRole('ADMIN')")
	public void create(TopicCreateRequest request) {
		TopicEntity entity = TopicEntity.builder()
				.name(request.getName())
				.build();
		topicRepository.save(entity);
	}

	@CacheEvict(value = "topics", allEntries = true)
	@PreAuthorize("hasRole('ADMIN')")
	public void update(int topicId, TopicUpdateRequest request) {
		TopicEntity entity = topicRepository.findById(topicId).orElse(null);
		entity.updateName(request.getName());
	}

	// 관리자에 의해 관리되고 매 페이지마다 호출하므로 캐싱
	@Cacheable("topics")
	public List<TopicDetailResponse> getListAll() {
		List<TopicEntity> entities = topicRepository.findAll();
		List<TopicDetailResponse> topics = new ArrayList<>();
		for (TopicEntity entity : entities) {
			topics.add(
					TopicDetailResponse.builder()
							.topicId(entity.getTopicId())
							.name(entity.getName())
							.build());
		}
		return topics;
	}

}
