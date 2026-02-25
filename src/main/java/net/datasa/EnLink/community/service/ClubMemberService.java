package net.datasa.EnLink.community.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.dto.response.ClubDetailResponse;
import net.datasa.EnLink.community.dto.response.ClubMemberHistoryResponse;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.member.repository.MemberRepository;
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
	
	@Transactional(readOnly = true)
	public Map<String, List<ClubDetailResponse>> getMyClubs(String memberId) {
		
		if (!memberRepository.existsById(memberId)) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}
		
		Map<String, List<ClubDetailResponse>> myClubsMap = new HashMap<>();
		
		// ✅ 1. 내가 현재 'ACTIVE' 멤버로 속해 있는 모든 모임 멤버십을 가져옵니다.
		// (모임의 상태가 ACTIVE든 DELETED_PENDING이든 나는 일단 ACTIVE 멤버니까요)
		List<ClubMemberEntity> allMyActiveMemberships = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "ACTIVE");
		
		// 1. 내가 만든 모임 (OWNER)
		// 💡 팁: 리스트를 한 번만 가져와서 스트림으로 거르는 게 DB 부하가 적습니다.
		myClubsMap.put("ownedClubs", allMyActiveMemberships.stream()
				.filter(e -> "OWNER".equals(e.getRole()))
				.map(this::convertToDetailResponse) // ✅ MemberEntity를 받는 오버로딩 메서드 사용
				.toList());
		
		// 2. 참여 중인 모임 (MANAGER, MEMBER)
		myClubsMap.put("activeClubs", allMyActiveMemberships.stream()
				.filter(e -> List.of("MANAGER", "MEMBER").contains(e.getRole()))
				.map(this::convertToDetailResponse)
				.toList());
		
		// 3. 신청 중인 모임 (PENDING)
		// 💡 신청 중인 모임은 별도의 조회가 필요합니다 (내 상태가 PENDING이니까요)
		List<ClubMemberEntity> pendingEntities = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "PENDING");
		myClubsMap.put("pendingClubs", pendingEntities.stream()
				.map(this::convertToDetailResponse)
				.toList());
		
		return myClubsMap;
	}
	
	
	/** * entity -> DTO 변환 (인원수 로직 보완)
	 */
	private ClubDetailResponse convertToDetailResponse(ClubMemberEntity entity) {
		
		ClubEntity club = entity.getClub();
		
		long activeCount = club.getMembers().stream()
				.filter(m -> "ACTIVE".equals(m.getStatus()))
				.count();
		
		// 2. DTO에 필요한 정보만 쏙쏙 뽑아서 빌더 또는 생성자로 세팅
		return ClubDetailResponse.builder()
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
	public List<ClubMemberHistoryResponse> getMemberImportantHistory(Integer clubId, String memberId) {
		List<ClubMemberHistoryEntity> historyEntities = clubMemberHistoryRepository
				.findByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(clubId, memberId);
		
		return historyEntities.stream()
				.filter(h -> java.util.Arrays.asList("EXIT", "BANNED").contains(h.getActionType()))
				.map(ClubMemberHistoryResponse::fromEntity)
				.toList();
	}
	
	//멤버가 모임에 속해있는지 확인
	public boolean checkClubMember(Integer clubId, String memberId){
		ClubMemberEntity entity = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId).orElse(null);
		return entity != null;
	}
}
