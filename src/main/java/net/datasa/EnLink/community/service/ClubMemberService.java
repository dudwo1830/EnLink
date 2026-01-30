package net.datasa.EnLink.community.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
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
	
	private final ClubMemberRepository clubMemberRepository;
	
	@Transactional(readOnly = true)
	public Map<String, List<ClubDTO>> getMyClubs(String memberId) {
		Map<String, List<ClubDTO>> myClubsMap = new HashMap<>();
		
		// 1. 내가 만든 모임 (OWNER)
		List<ClubMemberEntity> ownerEntities = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "ACTIVE");
		myClubsMap.put("ownedClubs", ownerEntities.stream()
				.filter(e -> "OWNER".equals(e.getRole()))
				.map(this::convertToDTO).toList());
		
		// 2. 참여 중인 모임 (ACTIVE & MEMBER/MANAGER)
		myClubsMap.put("activeClubs", ownerEntities.stream()
				.filter(e -> !"OWNER".equals(e.getRole()))
				.map(this::convertToDTO).toList());
		
		// 3. 신청 중인 모임 (PENDING)
		List<ClubMemberEntity> pendingEntities = clubMemberRepository.findByMember_MemberIdAndStatus(memberId, "PENDING");
		myClubsMap.put("pendingClubs", pendingEntities.stream()
				.map(this::convertToDTO).toList());
		
		return myClubsMap;
	}
	
	// 가입 신청 현황(PENDING) 페이징 조회
	public Page<ClubDTO> getPendingClubs(String loginId, int page) {
		// 10개씩, 가입 신청일 기준 최신순(예시)으로 페이징 설정
		Pageable pageable = PageRequest.of(page, 10, Sort.by("joinDate").descending());
		
		// Repository에서 Page<ClubMemberEntity>를 가져온 뒤 DTO로 변환
		Page<ClubMemberEntity> pendingEntities = clubMemberRepository
				.findByMember_MemberIdAndStatus(loginId, "PENDING", pageable);
		
		return pendingEntities.map(this::convertToDTO);
	}
	
	
	
	/** entity -> DTO 변환 로직 */
	private ClubDTO convertToDTO(ClubMemberEntity entity) {
		// 1. 실제 모임 정보는 entity.getClub()에 들어있음
		ClubEntity club = entity.getClub();
		
		// 2. DTO에 필요한 정보만 쏙쏙 뽑아서 빌더 또는 생성자로 세팅
		return ClubDTO.builder()
				.clubId(club.getClubId())
				.name(club.getName())
				.description(club.getDescription())
				.imageUrl(club.getImageUrl())
				.maxMember(club.getMaxMember())
				// 현재 인원수는 계산 로직이 있다면 여기서 세팅 (예: club.getMembers().size())
				.currentMemberCount(club.getMembers() != null ? club.getMembers().size() : 0)
				.role(entity.getRole())      // 현재 로그인 유저의 권한
				.status(entity.getStatus())  // 현재 로그인 유저의 가입 상태
				.build();
	}
}
