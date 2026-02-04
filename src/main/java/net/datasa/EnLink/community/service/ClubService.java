package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.city.repository.CityRepository;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubMemberDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
	private final ClubManageService clubManageService;
	private final TopicRepository topicRepository;
	private final CityRepository cityRepository;

	@Value("${file.upload.path}")
	private String uploadPath;

	/**
	 * 모임 생성 (이미지 처리 포함)
	 */
	public void createClub(ClubDTO clubDTO, String loginMemberId) {
		if (clubDTO.getMaxMember() % 10 != 0) {
			throw new IllegalArgumentException("최대 인원은 10단위로 설정해야 합니다.");
		}

		MemberEntity loginMember = memberRepository.findById(loginMemberId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		String imageUrl = "/images/default_club.jpg";
		if (clubDTO.getUploadFile() != null && !clubDTO.getUploadFile().isEmpty()) {
			try {
				String savedFileName = System.currentTimeMillis() + "_" + clubDTO.getUploadFile().getOriginalFilename();
				Path path = Paths.get(uploadPath, savedFileName);
				Files.copy(clubDTO.getUploadFile().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				imageUrl = "/images/" + savedFileName;
			} catch (IOException e) {
				throw new RuntimeException("이미지 저장 실패", e);
			}
		}

		TopicEntity topicEntity = topicRepository.findById(clubDTO.getTopicId())
				.orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
		CityEntity cityEntity = cityRepository.findById(clubDTO.getCityId())
				.orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

		ClubEntity club = ClubEntity.builder()
				.name(clubDTO.getName())
				.description(clubDTO.getDescription())
				.maxMember(clubDTO.getMaxMember())
				.topic(topicEntity)
				.city(cityEntity)
				.joinQuestion(clubDTO.getJoinQuestion())
				.imageUrl(imageUrl)
				.status("ACTIVE")
				.build();
		clubRepository.save(club);

		ClubMemberEntity member = ClubMemberEntity.builder()
				.club(club)
				.member(loginMember)
				.role("OWNER")
				.status("ACTIVE")
				.build();
		clubMemberRepository.save(member);

		ClubMemberHistoryEntity history = ClubMemberHistoryEntity.builder()
				.club(club)
				.targetMember(loginMember)
				.actorMember(loginMember)
				.actionType("JOIN_APPROVE")
				.description("모임 생성 및 모임장 등록")
				.build();
		clubMemberHistoryRepository.save(history);
	}

	/**
	 * 활성화된 모임 목록 조회 (인원수 카운트 포함)
	 */
	@Transactional(readOnly = true)
	public List<ClubDTO> getClubList() {
		return clubRepository.findByStatus("ACTIVE").stream()
				.map(entity -> {
					ClubDTO dto = convertToDTO(entity);
					int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(entity.getClubId(), "ACTIVE");
					dto.setCurrentMemberCount(currentCount);
					return dto;
				}).collect(Collectors.toList());
	}

	/**
	 * 모임 상세 조회
	 */
	@Transactional(readOnly = true)
	public ClubDTO getClubDetail(Integer clubId) {
		ClubEntity clubEntity = clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));
		ClubDTO dto = convertToDTO(clubEntity);
		int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		dto.setCurrentMemberCount(currentCount);
		return dto;
	}

	/**
	 * 가입 신청 처리 (재가입 로직 포함)
	 */
	public void applyToClub(Integer clubId, String memberId, String answerText) {
		ClubEntity club = clubRepository.findById(clubId).orElseThrow();
		MemberEntity member = memberRepository.findById(memberId).orElseThrow();

		Optional<ClubMemberEntity> existingMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId,
				memberId);

		if (existingMember.isPresent()) {
			ClubMemberEntity clubMember = existingMember.get();
			if ("ACTIVE".equals(clubMember.getStatus()))
				throw new RuntimeException("이미 가입된 멤버입니다.");
			if ("PENDING".equals(clubMember.getStatus()))
				throw new RuntimeException("이미 가입 신청 중입니다.");
			if ("BANNED".equals(clubMember.getStatus()))
				throw new RuntimeException("제명된 회원은 재가입이 불가능합니다.");

			// EXIT 상태면 PENDING으로 변경
			clubMember.setStatus("PENDING");
			clubMember.setRole("MEMBER");
		} else {
			clubMemberRepository.save(ClubMemberEntity.builder()
					.club(club).member(member).role("MEMBER").status("PENDING").build());
		}

		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		clubAnswerRepository.save(ClubJoinAnswerEntity.builder()
				.clubId(clubId).memberId(memberId).answerText(answerText).build());
	}

	/**
	 * 모임 탈퇴 (이력 남기기 포함)
	 */
	public void leaveClub(Integer clubId, String memberId) {
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("가입 정보가 없습니다."));

		if ("OWNER".equals(member.getRole()))
			throw new RuntimeException("모임장은 탈퇴할 수 없습니다.");

		member.setStatus("EXIT");
		member.setRole("MEMBER");
		clubManageService.leaveHistory(clubId, memberId, memberId, "EXIT", "자진 탈퇴");
	}

	/**
	 * 가입신청 취소
	 */
	public void cancelApplication(Integer clubId, String loginId) {
		// 1. 신청 내역 조회
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, loginId)
				.orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

		// 2. 답변 삭제 (가입 신청 시 썼던 질문 답변 데이터 제거)
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, loginId);

		// 3. 멤버 데이터 삭제 (PENDING 상태인 신청 줄을 아예 삭제)
		clubMemberRepository.delete(member);

		log.info("가입 신청 취소 완료(데이터 삭제): 모임ID={}, 멤버ID={}", clubId, loginId);
	}

	// --- 유틸리티 메서드 ---
	private ClubDTO convertToDTO(ClubEntity entity) {
		ClubDTO dto = ClubDTO.builder()
				.clubId(entity.getClubId()).name(entity.getName()).description(entity.getDescription())
				.maxMember(entity.getMaxMember()).topicId(entity.getTopic().getTopicId()).cityId(entity.getCity().getCityId())
				.imageUrl(entity.getImageUrl()).status(entity.getStatus()).joinQuestion(entity.getJoinQuestion())
				.build();

		if ("DELETED_PENDING".equals(entity.getStatus()) && entity.getDeletedAt() != null) {
			LocalDateTime expiryDate = entity.getDeletedAt().plusDays(7);
			Duration duration = Duration.between(LocalDateTime.now(), expiryDate);
			dto.setRemainingTime(
					duration.isNegative() ? "곧 삭제 예정" : duration.toDays() + "일 " + duration.toHoursPart() + "시간 남음");
		}
		return dto;
	}

	public String getApplicationStatus(Integer clubId, String loginId) {
		return clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, loginId)
				.map(ClubMemberEntity::getStatus) // status 컬럼(ACTIVE, PENDING 등)을 꺼냄
				.orElse(null);
	}

	public List<ClubMemberDTO> getActiveMembers(String clubId) {
		// 1. 해당 클럽의 ACTIVE 상태인 멤버들 조회
		List<ClubMemberEntity> entities = clubMemberRepository.findByMember_MemberIdAndStatus(clubId, "ACTIVE");

		return entities.stream()
				.map(entity -> ClubMemberDTO.builder()
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
}
