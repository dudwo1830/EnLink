package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	/**
	 * 모임 생성 (이미지 처리 포함)
	 */
	@Transactional
	public Integer createClub(ClubDTO clubDTO, String loginMemberId) {
		
		validateCreateClub(clubDTO, loginMemberId);
		
		MemberEntity loginMember = memberRepository.findById(loginMemberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		
		String imageUrl = storeUploadFile(clubDTO.getUploadFile());
		if (imageUrl == null) {
			imageUrl = "/images/default_club.jpg";
		}
		
		ClubEntity club = ClubEntity.builder()
				.name(clubDTO.getName())
				.description(clubDTO.getDescription())
				.maxMember(clubDTO.getMaxMember())
				.topicId(clubDTO.getTopicId())
				.cityId(clubDTO.getCityId())
				.joinQuestion(clubDTO.getJoinQuestion())
				.imageUrl(imageUrl)
				.status("ACTIVE")
				.build();
		
		clubRepository.save(club);
		registerOwner(club, loginMember);
		
		return club.getClubId();
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
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
		
		ClubDTO dto = convertToDTO(clubEntity);
		dto.setStatus(clubEntity.getStatus());
		
		if ("DELETE_PENDING".equals(clubEntity.getStatus()) && clubEntity.getDeletedAt() != null) {
			// 삭제 예정일 = 삭제 신청일 + 7일
			java.time.LocalDateTime expiryDate = clubEntity.getDeletedAt().plusDays(7);
			java.time.Duration duration = java.time.Duration.between(java.time.LocalDateTime.now(), expiryDate);
			
			if (duration.isNegative()) {
				dto.setRemainingTime("삭제 처리 중...");
			} else {
				long days = duration.toDays();
				long hours = duration.toHoursPart();
				dto.setRemainingTime(days + "일 " + hours + "시간 남음");
			}
		}
		
		int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		dto.setCurrentMemberCount(currentCount);
		return dto;
	}
	
	/**
	 * 가입 신청 처리 (재가입 로직 포함)
	 */
	public void applyToClub(Integer clubId, String memberId, String answerText) {
		
		long activeCount = clubMemberRepository.countByMember_MemberIdAndStatus(memberId, "ACTIVE");
		
		if (activeCount >= 5) {
			throw new BusinessException(ErrorCode.JOIN_LIMIT_EXCEEDED);
		}
		
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
		
		MemberEntity member = memberRepository.findById(memberId).
				orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		
		Optional<ClubMemberEntity> existingMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId);
		
		if (existingMember.isPresent()) {
			ClubMemberEntity clubMember = existingMember.get();
			String currentStatus = clubMember.getStatus();
			
			if ("ACTIVE".equals(currentStatus) || "PENDING".equals(currentStatus)) {
				throw new BusinessException(ErrorCode.ALREADY_JOINED_OR_PENDING);
			}
			
			if ("EXIT".equals(clubMember.getStatus()) || "BANNED".equals(clubMember.getStatus())) {
				clubMember.setStatus("PENDING");
				clubMember.setRole("MEMBER");
				clubMember.setJoinedAt(LocalDateTime.now());
			}
			
		} else {
			clubMemberRepository.save(ClubMemberEntity.builder()
					.club(club)
					.member(member)
					.role("MEMBER")
					.status("PENDING")
					.build());
		}
		
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
	public List<ClubMemberDTO> getActiveMembers(Integer clubId) {
		// 1. 해당 클럽의 ACTIVE 상태인 멤버들 조회
		List<ClubMemberEntity> entities = clubMemberRepository.findByClub_ClubIdAndStatusOrderByRoleAsc(clubId, "ACTIVE");
		
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
					if (m1.getRole().equals("OWNER")) return -1;
					if (m2.getRole().equals("OWNER")) return 1;
					if (m1.getRole().equals("MANAGER")) return -1;
					if (m2.getRole().equals("MANAGER")) return 1;
					return 0;
				})
				.collect(Collectors.toList());
	}
	
	/**
	 * 유틸리티 메서드 entity -> DTO
	 * */
	private ClubDTO convertToDTO(ClubEntity entity) {
		ClubDTO dto = ClubDTO.builder()
				.clubId(entity.getClubId()).name(entity.getName()).description(entity.getDescription())
				.maxMember(entity.getMaxMember()).topicId(entity.getTopicId()).cityId(entity.getCityId())
				.imageUrl(entity.getImageUrl()).status(entity.getStatus()).joinQuestion(entity.getJoinQuestion())
				.build();
		
		if ("DELETED_PENDING".equals(entity.getStatus()) && entity.getDeletedAt() != null) {
			LocalDateTime expiryDate = entity.getDeletedAt().plusDays(7);
			Duration duration = Duration.between(LocalDateTime.now(), expiryDate);
			dto.setRemainingTime(duration.isNegative() ? "곧 삭제 예정" : duration.toDays() + "일 " + duration.toHoursPart() + "시간 남음");
		}
		return dto;
	}
	
	/**
 	* 모임 생성 유효성 검증
 	*/
	private void validateCreateClub(ClubDTO clubDTO, String loginMemberId) {
	if (clubRepository.existsByName(clubDTO.getName())) {
		throw new BusinessException(ErrorCode.DUPLICATE_NAME);
	}
	if (clubDTO.getMaxMember() % 10 != 0) {
		throw new BusinessException(ErrorCode.INVALID_MAX_MEMBER);
	}
		
		long totalActiveCount = clubMemberRepository.countByMember_MemberIdAndStatus(loginMemberId, "ACTIVE");
		if (totalActiveCount >= 5) {
			throw new BusinessException(ErrorCode.JOIN_LIMIT_EXCEEDED);
	}
}
	
	/**
	 * 파일 저장 로직 (자동 폴더 생성 포함)
	 */
	private String storeUploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		
		try {
			Path root = Paths.get(uploadPath);
			if (!Files.exists(root)) Files.createDirectories(root);
			
			String savedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			Files.copy(file.getInputStream(), root.resolve(savedFileName), StandardCopyOption.REPLACE_EXISTING);
			
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
}
