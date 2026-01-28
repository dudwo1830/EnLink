package net.datasa.EnLink.topic.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.topic.dto.TopicDTO;
import net.datasa.EnLink.topic.entity.TopicEntity;
import net.datasa.EnLink.topic.repository.TopicRepository;

@Transactional
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TopicService {
	private final TopicRepository topicRepository;

	public void create(TopicDTO request) {
		TopicEntity entity = TopicEntity.builder()
				.name(request.getName())
				.build();
		topicRepository.save(entity);
	}

	public void update(int topicId, TopicDTO request) {
		TopicEntity entity = topicRepository.findById(topicId).orElse(null);
		entity.updateName(request.getName());
	}

	public List<TopicDTO> getListAll() {
		List<TopicEntity> entities = topicRepository.findAll();
		List<TopicDTO> topics = new ArrayList<>();
		for (TopicEntity entity : entities) {
			topics.add(
					TopicDTO.builder()
							.topicId(entity.getTopicId())
							.name(entity.getName())
							.build());
		}
		return topics;
	}

}
