package net.datasa.EnLink.community.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubMemberHistoryDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClubMemberService {
	
	private final MemberRepository memberRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final ClubMemberHistoryService clubMemberHistoryService;
	
	/**
	 * 내 모임 목록 조회
	 */
	@Transactional(readOnly = true)
	public Map<String, List<ClubDTO>> getMyClubs(String memberId) {
		
		if (!memberRepository.existsById(memberId)) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}
		
		Map<String, List<ClubDTO>> myClubsMap = new HashMap<>();
		
		List<ClubMemberEntity> activeEntities = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "ACTIVE");
		
		// 1. 내가 만든 모임
		myClubsMap.put("ownedClubs", activeEntities.stream()
				.filter(e -> "OWNER".equals(e.getRole()))
				.map(this::convertToDTO).toList());
		
		// 2. 참여 중인 모임
		myClubsMap.put("activeClubs", activeEntities.stream()
				.filter(e -> !"OWNER".equals(e.getRole()))
				.map(this::convertToDTO).toList());
		
		// 3. 신청 중인 모임
		List<ClubMemberEntity> pendingEntities = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "PENDING");
		myClubsMap.put("pendingClubs", pendingEntities.stream()
				.map(this::convertToDTO).toList());
		
		return myClubsMap;
	}
	
	/**
	 * 가입 신청 현황 페이징 조회
	 */
	public Page<ClubDTO> getPendingClubs(String loginId, int page) {
		
		Pageable pageable = PageRequest.of(page, 10, Sort.by("joinDate").descending());
		
		Page<ClubMemberEntity> pendingEntities = clubMemberRepository
				.findByMember_MemberIdAndStatus(loginId, "PENDING", pageable);
		
		return pendingEntities.map(this::convertToDTO);
	}
	
	
	/** * entity -> DTO 변환 (인원수 로직 보완)
	 */
	private ClubDTO convertToDTO(ClubMemberEntity entity) {
		
		ClubEntity club = entity.getClub();
		
		long activeCount = club.getMembers().stream()
				.filter(m -> "ACTIVE".equals(m.getStatus()))
				.count();
		
		// 2. DTO에 필요한 정보만 쏙쏙 뽑아서 빌더 또는 생성자로 세팅
		return ClubDTO.builder()
				.clubId(club.getClubId())
				.name(club.getName())
				.description(club.getDescription())
				.imageUrl(club.getImageUrl())
				.maxMember(club.getMaxMember())
				.currentMemberCount((int) activeCount)
				.role(entity.getRole())
				.status(club.getStatus())
				.build();
	}
	
	@Transactional
	public void leaveClub(Integer clubId, String memberId, String description) {
		// 1. 해당 멤버 존재 여부 확인
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER));
		
		// 2. 권한 및 상태 검증
		if ("OWNER".equals(member.getRole())) {
			throw new BusinessException(ErrorCode.OWNER_CANNOT_LEAVE);
		}
		if (!"ACTIVE".equals(member.getStatus())) {
			throw new BusinessException(ErrorCode.ALREADY_INACTIVE);
		}
		
		member.setStatus("EXIT");
		member.setRole("MEMBER");
		
		clubMemberHistoryService.leaveHistory(clubId, memberId, memberId, "EXIT", description);
		
		log.info("[모임 탈퇴 성공] 모임: {}, 유저: {}, 권한 초기화 완료", clubId, memberId);
	}
	
	
	
	/**
	 * 특정 멤버의 중요 이력(탈퇴, 제명)만 필터링하여 조회
	 */
	public List<ClubMemberHistoryDTO> getMemberImportantHistory(Integer clubId, String memberId) {
		List<ClubMemberHistoryEntity> historyEntities = clubMemberHistoryRepository
				.findByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(clubId, memberId);
		
		return historyEntities.stream()
				.filter(h -> java.util.Arrays.asList("EXIT", "BANNED").contains(h.getActionType()))
				.map(ClubMemberHistoryDTO::fromEntity)
				.toList();
	}
	
	/**
	 * 특정 멤버의 전체 이력만 필터링하여 조회
	 */
	public List<ClubMemberHistoryDTO> getMemberAllHistory(Integer clubId, String memberId) {
		// 1. 해당 유저의 모든 이력을 최신순으로 조회
		List<ClubMemberHistoryEntity> historyEntities = clubMemberHistoryRepository
				.findByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(clubId, memberId);
		
		return historyEntities.stream()
				.map(ClubMemberHistoryDTO::fromEntity)
				.toList();
	}
}
