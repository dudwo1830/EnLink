package net.datasa.EnLink.member.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.dto.response.CityDetailResponse;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.repository.CityRepository;
import net.datasa.EnLink.common.Utils.MaskingUtils;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.dto.request.MemberUpdateRequest;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.dto.response.MemberUpdateResponse;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.entity.MemberRole;
import net.datasa.EnLink.member.entity.MemberStatus;
import net.datasa.EnLink.member.repository.MemberRepository;
import net.datasa.EnLink.membercity.entity.MemberCityEntity;
import net.datasa.EnLink.membercity.repository.MemberCityRepository;
import net.datasa.EnLink.membertopic.entity.MemberTopicEntity;
import net.datasa.EnLink.membertopic.repository.MemberTopicRepository;
import net.datasa.EnLink.topic.entity.TopicEntity;
import net.datasa.EnLink.topic.repository.TopicRepository;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {
	private final BCryptPasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final TopicRepository topicRepository;
	private final MemberTopicRepository memberTopicRepository;
	private final CityRepository cityRepository;
	private final MemberCityRepository memberCityRepository;

	/**
	 * 회원 가입
	 * 
	 * @param request
	 */
	public void create(MemberCreateRequest request) {
		if (!request.getPassword().equals(request.getRePassword())) {
			throw new BusinessException(ErrorCode.USER_PASSWORD_MISMATCH, MemberCreateRequest.FILED_RE_PASSWORD);
		}
		Boolean isUsed = memberRepository.existsById(request.getMemberId());
		if (isUsed) {
			throw new BusinessException(ErrorCode.ALREADY_USED_ID, MemberCreateRequest.FILED_MEMBER_ID);
		}
		MemberEntity entity = MemberEntity.builder()
				.memberId(request.getMemberId())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.email(request.getEmail())
				.birth(request.getBirth())
				.status(MemberStatus.ACTIVE)
				.role(MemberRole.USER)
				.build();
		memberRepository.save(entity);
	}

	/**
	 * 회원 정보 수정
	 * 
	 * @param request
	 * @param memberId
	 * @return
	 */
	@PreAuthorize("#memberId == principal.memberId")
	public void update(MemberUpdateRequest request, String memberId) {
		MemberEntity entity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		// 기존 비밀번호 확인
		if (entity.getPassword().equals(passwordEncoder.encode(request.getPassword()))) {
			throw new BusinessException(ErrorCode.USER_PASSWORD_MISMATCH, MemberUpdateRequest.FILED_RE_PASSWORD);
		}
		// 새 비밀번호가 존재할 경우
		if (!request.getNewPassword().isEmpty()) {
			if (request.getNewPassword().equals(request.getRePassword())) {
				entity.updatePassword(passwordEncoder.encode(request.getNewPassword()));
			} else {
				throw new BusinessException(ErrorCode.USER_PASSWORD_MISMATCH, MemberUpdateRequest.FILED_RE_PASSWORD);
			}
		}
		entity.updateProfile(request.getName(), request.getEmail(), request.getBirth());
	}

	/**
	 * 회원 정보 조회
	 * 
	 * @param memberId
	 * @return
	 */
	public MemberDetailResponse read(String memberId) {
		String locale = LocaleContextHolder.getLocale().getLanguage();
		MemberEntity entity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		String topic = String
				.join(", ", entity.getMemberTopics().stream()
						.map(memberTopic -> memberTopic.getTopic().getLocalizedName(locale)).toList());
		String city = (entity.getCity() != null)
				? entity.getCity().getRegion().getNameLocal() + " " + entity.getCity().getNameLocal()
				: "";
		return MemberDetailResponse.builder()
				.memberId(entity.getMemberId())
				.name(entity.getName())
				.email(MaskingUtils.maskEmail(entity.getEmail()))
				.birth(entity.getBirth())
				.topic(topic)
				.city(city)
				.build();
	}

	/**
	 * 회원 탈퇴
	 * 
	 * @param memberId
	 */
	public void delete(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		entity.updateStatus(MemberStatus.INACTIVE);
	}

	/**
	 * 회원 수정시 정보 조회
	 * 
	 * @param memberId
	 * @return
	 */
	@PreAuthorize("#memberId == principal.memberId")
	public MemberUpdateResponse edit(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		return MemberUpdateResponse.builder()
				.memberId(entity.getMemberId())
				.name(entity.getName())
				.email(entity.getEmail())
				.birth(entity.getBirth())
				.build();
	}

	/**
	 * 멤버의 관심 주제 재설정
	 * 
	 * @param memberId
	 * @param newTopicIds
	 */
	@PreAuthorize("#memberId == principal.memberId")
	public void replaceTopics(String memberId, List<Integer> newTopicIds) {
		// 회원 정보
		MemberEntity memberEntity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		// 변경할 주제 리스트
		List<TopicEntity> newTopics = topicRepository.findAllById(newTopicIds);
		// List -> Set 변환
		Set<Integer> newTopicIdSet = new HashSet<>(newTopicIds);

		// 기존 회원_주제 데이터에서 주제 id만 가져옴
		Set<Integer> currentTopicIdSet = memberTopicRepository.findTopicIdsByMember(memberEntity);

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

	/**
	 * 회원의 관심 지역 설정 처리
	 * 
	 * @param memberId
	 * @param cityId
	 */
	@PreAuthorize("#memberId == principal.memberId")
	public void updateCity(String memberId, Integer cityId) {
		Optional<MemberCityEntity> opt = memberCityRepository.findByMember_MemberId(memberId);
		// 최초 수정시는 null
		MemberCityEntity entity = opt.orElse(null);

		CityEntity cityEntity = cityRepository.findById(cityId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
		// 최초 수정시 추가
		if (entity == null) {
			MemberEntity memberEntity = memberRepository.findById(memberId)
					.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
			memberCityRepository.save(new MemberCityEntity(memberEntity, cityEntity));
		} else {
			// 이후는 수정
			entity.updateCity(cityEntity);
		}
	}

	/**
	 * 회원의 관심 지역 조회
	 * 
	 * @param memberId
	 * @return
	 */
	@PreAuthorize("#memberId == principal.memberId")
	public CityDetailResponse getMemberCity(String memberId) {
		MemberEntity memberEntity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		if (memberEntity.getCity() != null) {
			return new CityDetailResponse(
					memberEntity.getCity().getCityId(),
					memberEntity.getCity().getNameLocal(),
					memberEntity.getCity().getRegion().getNameLocal() + " " + memberEntity.getCity().getNameLocal());
		} else {
			return null;
		}
	}
}