package net.datasa.EnLink.membertopic.service;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
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
		String locale = LocaleContextHolder.getLocale().getLanguage();
		return memberTopicRepository.findAllWithCheck(memberId, locale);
	}
}
