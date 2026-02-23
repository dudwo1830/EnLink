package net.datasa.EnLink.topic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.locale.LocaleType;
import net.datasa.EnLink.topic.dto.request.TopicCreateRequest;
import net.datasa.EnLink.topic.dto.request.TopicUpdateRequest;
import net.datasa.EnLink.topic.dto.response.TopicDetailResponse;
import net.datasa.EnLink.topic.dto.response.TopicUpdateResponse;
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
				.nameKo(request.getNameKo())
				.nameJa(request.getNameJa())
				.build();
		topicRepository.save(entity);
	}

	@CacheEvict(value = "topics", allEntries = true)
	@PreAuthorize("hasRole('ADMIN')")
	public void update(int topicId, TopicUpdateRequest request) {
		TopicEntity entity = topicRepository.findById(topicId).orElse(null);
		entity.updateName(request.getNameKo(), request.getNameJa());
	}

	// 관리자에 의해 관리되고 매 페이지마다 호출하므로 캐싱
	@Cacheable(value = "topics", key = "T(org.springframework.context.i18n.LocaleContextHolder).getLocale().language")
	public List<TopicDetailResponse> getListAll() {
		Locale locale = LocaleContextHolder.getLocale();
		String lang = LocaleType.from(locale).getCode();
		System.out.println(lang);
		List<TopicEntity> entities = topicRepository.findAll();
		List<TopicDetailResponse> topics = new ArrayList<>();
		for (TopicEntity entity : entities) {
			topics.add(
					TopicDetailResponse.builder()
							.topicId(entity.getTopicId())
							.name(entity.getLocalizedName(lang))
							.build());
		}
		return topics;
	}

	public List<TopicUpdateResponse> getUpdateListAll() {
		List<TopicEntity> entities = topicRepository.findAll();
		List<TopicUpdateResponse> topics = new ArrayList<>();
		for (TopicEntity entity : entities) {
			topics.add(
					TopicUpdateResponse.builder()
							.topicId(entity.getTopicId())
							.nameKo(entity.getNameKo())
							.nameJa(entity.getNameJa())
							.build());
		}
		return topics;
	}
}
