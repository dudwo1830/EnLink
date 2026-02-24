package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.repository.CityRepository;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.dto.ClubSummaryResponse;
import net.datasa.EnLink.community.dto.request.ClubCreateRequest;
import net.datasa.EnLink.community.dto.response.ClubDetailResponse;
import net.datasa.EnLink.community.dto.response.ClubListResponse;
import net.datasa.EnLink.community.dto.response.ClubMemberResponse;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubJoinAnswerEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import net.datasa.EnLink.community.repository.ClubAnswerRepository;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import net.datasa.EnLink.topic.entity.TopicEntity;
import net.datasa.EnLink.topic.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

	private final ClubRepository clubRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final ClubAnswerRepository clubAnswerRepository;
	private final MemberRepository memberRepository;
	private final TopicRepository topicRepository;
	private final CityRepository cityRepository;
	
	@Value("${file.upload.path}")
	private String uploadPath;

	/**
	 * 모임 생성 (이미지 처리 포함)
	 */
	@Transactional
	public Integer createClub(ClubCreateRequest clubCreateDTO, String loginMemberId) {
		String locale = LocaleContextHolder.getLocale().getLanguage();

		validateCreateClub(clubCreateDTO, loginMemberId);
		
		MemberEntity loginMember = memberRepository.findById(loginMemberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		
		String imageUrl = storeUploadFile(clubCreateDTO.getUploadFile());
		if (imageUrl == null) {
			imageUrl = "/images/default_club.jpg";
		}

		TopicEntity topic = topicRepository.findById(clubCreateDTO.getTopicId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));
		CityEntity city = cityRepository.findById(clubCreateDTO.getCityId())
				.orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

		ClubEntity club = ClubEntity.builder()
				.name(clubCreateDTO.getName())
				.description(clubCreateDTO.getDescription())
				.maxMember(clubCreateDTO.getMaxMember())
				.topic(topic)
				.city(city)
				.joinQuestion(clubCreateDTO.getJoinQuestion())
				.imageUrl(imageUrl)
				.status("ACTIVE")
				.locale(locale)
				.build();
		
		clubRepository.save(club);
		registerOwner(club, loginMember);
		
		return club.getClubId();
	}

	/**
	 * 활성화된 모임 목록 조회 (인원수 카운트 포함)
	 */
	@Transactional(readOnly = true)
	public List<ClubListResponse> getClubList() {
		
		
		return clubRepository.findByStatus("ACTIVE").stream()
				.map(this::convertToListResponse)
				.collect(Collectors.toList());
	}
	
	/**
	 * 모임 상세 조회
	 */
	@Transactional(readOnly = true)
	public ClubDetailResponse getClubDetail(Integer clubId) {
		ClubEntity clubEntity = clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
		
		ClubDetailResponse response = convertToDetailResponse(clubEntity);
		
		// ✅ 2. 현재 인원 수 카운트 주입
		int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		response.setCurrentMemberCount(currentCount);
		
		return response;
	}
	
	/**
	 * 모임상세정보 변환
	 * */
	private ClubDetailResponse convertToDetailResponse(ClubEntity entity) {
		String locale = LocaleContextHolder.getLocale().getLanguage();
		String remainingTime = null;
		if ("DELETED_PENDING".equals(entity.getStatus()) && entity.getDeletedAt() != null) {
			LocalDateTime expiryDate = entity.getDeletedAt().plusDays(7);
			Duration duration = Duration.between(LocalDateTime.now(), expiryDate);
			remainingTime = duration.isNegative() ? "삭제 처리 중..." :
					duration.toDays() + "일 " + duration.toHoursPart() + "시간 남음";
		}

		return ClubDetailResponse.builder()
				.clubId(entity.getClubId())
				.name(entity.getName())
				.description(entity.getDescription())
				.maxMember(entity.getMaxMember())
				.topicId(entity.getTopic().getTopicId())
				.topicName(entity.getTopic().getLocalizedName(locale))
				.cityId(entity.getCity().getCityId())
				.cityName(entity.getCity().getNameLocal())
				.imageUrl(entity.getImageUrl())
				.status(entity.getStatus())
				.joinQuestion(entity.getJoinQuestion())
				.remainingTime(remainingTime)
				.createdAt(entity.getCreatedAt())
				.build();
	}

	/**
	 * 신규 가입 신청을 처리합니다. (5+5 참여 쿼터제 적용)
	 * 탈퇴/제명 이력이 있는 경우 기존 데이터를 'PENDING' 상태로 갱신하여 재신청 처리합니다.
	 */
	public void applyToClub(Integer clubId, String memberId, String answerText) {
		// 1. [정책] 참여 쿼터 체크 (최대 5개, 삭제 대기 모임 포함)
		if (clubMemberRepository.countParticipantQuota(memberId) >= 5) {
			throw new BusinessException(ErrorCode.JOIN_LIMIT_EXCEEDED);
		}
		
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
		
		// 2. [정책] 삭제 진행 중인 모임은 신규 가입 불가
		if ("DELETED_PENDING".equals(club.getStatus())) {
			throw new BusinessException(ErrorCode.CLUB_NOT_FOUND);
		}
		
		MemberEntity member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		
		Optional<ClubMemberEntity> existingMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId);
		
		if (existingMember.isPresent()) {
			updateExistingMemberStatus(existingMember.get());
		} else {
			createNewMemberApplication(club, member);
		}
		
		// 신청 답변 갱신 (기존 답변 삭제 후 재생성)
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		clubAnswerRepository.save(ClubJoinAnswerEntity.builder()
				.clubId(clubId).memberId(memberId).answerText(answerText).build());
	}
	
	/**
	 * 가입신청 취소
	 * */
	public void cancelApplication(Integer clubId, String loginId){
			// 1. 신청 내역 조회
			ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, loginId)
					.orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));
			
			clubAnswerRepository.deleteByClubIdAndMemberId(clubId, loginId);
			
			clubMemberRepository.delete(member);
		}
		
	/**
	 * 활동중인 멤버 조회
	 * */
	public List<ClubMemberResponse> getActiveMembers(Integer clubId) {
		// 1. 해당 클럽의 ACTIVE 상태인 멤버들 조회
		List<ClubMemberEntity> entities = clubMemberRepository.findByClub_ClubIdAndStatusOrderByRoleAsc(clubId, "ACTIVE");
		
		return entities.stream()
				.map(entity -> ClubMemberResponse.builder()
						.cmId(entity.getCmId())
						.memberId(entity.getMember().getMemberId())
						.memberName(entity.getMember().getName())
						.role(entity.getRole())
						.joinedAt(entity.getJoinedAt())
						.build())
				// 2. 직급 순서 정렬 (OWNER -> MANAGER -> MEMBER 순)
				.sorted((m1, m2) -> {
					if (m1.getRole().equals("OWNER"))
						return -1;
					if (m2.getRole().equals("OWNER"))
						return 1;
					if (m1.getRole().equals("MANAGER"))
						return -1;
					if (m2.getRole().equals("MANAGER"))
						return 1;
					return 0;
				})
				.collect(Collectors.toList());
	}
	
	
	/**
 	* 모임 생성 유효성 검증
 	*/
	private void validateCreateClub(ClubCreateRequest requestDTO, String loginMemberId) {
	if (clubRepository.existsByName(requestDTO.getName())) {
		throw new BusinessException(ErrorCode.DUPLICATE_NAME);
	}
	if (requestDTO.getMaxMember() % 10 != 0) {
		throw new BusinessException(ErrorCode.INVALID_MAX_MEMBER);
	}
		
		long ownedClubCount = clubMemberRepository.countOwnerQuota(loginMemberId);
		
		if (ownedClubCount >= 5) {
			throw new BusinessException(ErrorCode.OWNER_LIMIT_EXCEEDED);
		}
}
	
	/**
	 * 파일 저장 로직 (UUID 적용으로 한글/특수문자 404 완벽 방어)
	 */
	private String storeUploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		
		try {
			Path root = Paths.get(uploadPath);
			if (!Files.exists(root)) {
				Files.createDirectories(root);
			}
			
			// 1. 원본 파일명에서 확장자만 안전하게 추출
			String originalFileName = file.getOriginalFilename();
			String extension = "";
			if (originalFileName != null && originalFileName.contains(".")) {
				extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}
			
			// 2. UUID + 타임스탬프 조합으로 영문/숫자 파일명 생성 (404 방지 핵심)
			// 예: 8f2d12..._170812345.jpg
			String savedFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
			
			// 3. 파일 저장
			Files.copy(file.getInputStream(), root.resolve(savedFileName), StandardCopyOption.REPLACE_EXISTING);
			
			// 4. DB에 저장될 경로 반환
			return "/images/" + savedFileName;
			
		} catch (IOException e) {
			log.error("이미지 저장 실패: {}", e.getMessage());
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 모임장 등록 및 히스토리 저장
	 */
	private void registerOwner(ClubEntity club, MemberEntity owner) {
		clubMemberRepository.save(ClubMemberEntity.builder()
				.club(club).member(owner).role("OWNER").status("ACTIVE").build());
		
		clubMemberHistoryRepository.save(ClubMemberHistoryEntity.builder()
				.club(club).targetMember(owner).actorMember(owner)
				.actionType("JOIN_APPROVE").description("모임 생성 및 모임장 등록").build());
	}


	
	private ClubListResponse convertToListResponse(ClubEntity entity) {
		int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(entity.getClubId(), "ACTIVE");
		
		return ClubListResponse.builder()
				.clubId(entity.getClubId())
				.name(entity.getName())
				.description(entity.getDescription()) // 목록에도 설명이 필요하다면 유지
				.imageUrl(entity.getImageUrl())
				.currentMemberCount(currentCount)
				.maxMember(entity.getMaxMember())
				.topicId(entity.getTopic().getTopicId())
				.cityId(entity.getCity().getCityId()) // 시티 맵 로직 유지
				.status(entity.getStatus())
				.build();
	}
	
	private void updateExistingMemberStatus(ClubMemberEntity clubMember) {
		String currentStatus = clubMember.getStatus();
		
		if ("ACTIVE".equals(currentStatus) || "PENDING".equals(currentStatus)) {
			throw new BusinessException(ErrorCode.ALREADY_JOINED_OR_PENDING);
		}
		
		if ("EXIT".equals(currentStatus) || "BANNED".equals(currentStatus)) {
			clubMember.setStatus("PENDING");
			clubMember.setRole("MEMBER");
			clubMember.setAppliedAt(LocalDateTime.now());
		}
	}
	
	private void createNewMemberApplication(ClubEntity club, MemberEntity member) {
		clubMemberRepository.save(ClubMemberEntity.builder()
				.club(club)
				.member(member)
				.role("MEMBER")
				.status("PENDING")
				.appliedAt(LocalDateTime.now())
				.build());
	}

	/**
	 * 모임 리스트 조회 및 페이징 처리, 검색
	 * 
	 * @param pageable
	 * @param search   검색할 내용
	 * @param topicId  주제 PK
	 * @param cityId   지역 PK
	 * @return
	 */
	public Slice<ClubSummaryResponse> getClubListBySlice(Pageable pageable, Integer cityId, Integer topicId,
			String search, Integer regionId) {
		String locale = LocaleContextHolder.getLocale().getLanguage();
		return clubRepository.searchClubs(pageable, cityId, topicId, search, regionId, locale);
	}

	public List<ClubSummaryResponse> getListByTopicId(Integer topicId){
		String locale = LocaleContextHolder.getLocale().getLanguage();
		return clubRepository.findClubSummary(topicId, locale);
	}
	
	/**
	 * 클럽명 중복체크 (생성)
	 * */
	public boolean existsByName(String name) {
		return clubRepository.existsByName(name);
	}
	
	/**
	 * 클럽명 중복체크 (수정)
	 * */
	public boolean isNameAvailableForEdit(String name, Integer clubId) {
		return !clubRepository.existsByNameAndClubIdNot(name, clubId);
	}
}
