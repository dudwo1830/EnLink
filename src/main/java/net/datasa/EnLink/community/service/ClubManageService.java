package net.datasa.EnLink.community.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClubManageService {
	
	private final ClubRepository clubRepository;
	private final ClubAnswerRepository clubAnswerRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final MemberRepository memberRepository;
	
	@Value("${file.upload.path:src/main/resources/static/images/}") // 파일 저장 경로 설정
	private String uploadPath;
	
	/**
	 * 모임 ID로 모임 엔티티를 직접 조회합니다.
	 */
	@Transactional(readOnly = true)
	public ClubEntity getClubById(Integer clubId) {
		return clubRepository.findById(clubId)
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 모임입니다."));
	}
	
	
	/**
	 * 모임 수정을 위해 기존 정보를 조회하고 DTO로 변환하여 반환 (Controller의 edit 메서드에서 호출)
	 */
	@Transactional(readOnly = true)
	public ClubDTO getClubForEdit(Integer clubId) {
		ClubEntity entity = getClubById(clubId);
		return convertToDTO(entity);
	}
	
	/**
	 * 모임의 기본 정보(이름, 설명, 인원, 질문)와 업로드된 파일을 처리하여 정보를 업데이트합니다.
	 */
	@Transactional
	public void updateClub(Integer id, ClubDTO clubDTO) {
		
		ClubEntity club = clubRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다. id=" + id));
		
		club.setName(clubDTO.getName());
		club.setDescription(clubDTO.getDescription());
		club.setMaxMember(clubDTO.getMaxMember());
		club.setJoinQuestion(clubDTO.getJoinQuestion());
		
		// ⭐ 이미지 수정 로직
		if (clubDTO.getUploadFile() != null && !clubDTO.getUploadFile().isEmpty()) {
			try {
				String savedFileName = System.currentTimeMillis() + "_" +
						clubDTO.getUploadFile().getOriginalFilename();
				
				Path path = Paths.get(uploadPath, savedFileName);
				Files.copy(
						clubDTO.getUploadFile().getInputStream(),
						path,
						StandardCopyOption.REPLACE_EXISTING
				);
				
				club.setImageUrl("/images/" + savedFileName);
			} catch (IOException e) {
				throw new RuntimeException("이미지 수정 실패", e);
			}
		}
	}
	
	/**
	 * 모임삭제
	 * */
	public void requestDeleteClub(Integer clubId) {
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));
		
		// 1. 상태를 '삭제 대기'로 변경
		club.setStatus("PENDING_DELETE");
		
		// 2. 삭제 요청 시간 기록 (현재 시간)
		club.setDeletedAt(LocalDateTime.now());
		
		clubRepository.save(club);
	}
	
	/**
	 * 모임 상태를 'DELETED_PENDING'으로 변경하고 삭제 시간을 기록하여 7일 유예 기간을 시작합니다.
	 */
	@Transactional
	public void deleteClub(Integer clubId) {
		// 1. 삭제할 모임 존재 여부 확인
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("삭제할 모임 정보를 찾을 수 없습니다."));
		
		// 2. 모임 상태를 '삭제 대기'로 변경
		club.setStatus("DELETED_PENDING");
		
		// 3. 삭제 요청 시간 기록 (영재님이 만든 deleted_at 컬럼 활용)
		// 이 시간을 기준으로 스케줄러가 7일을 계산합니다.
		club.setDeletedAt(LocalDateTime.now());
		
		// 4. 변경 내용 저장 (더티 체킹에 의해 자동 반영되지만 명시적으로 호출 가능)
		clubRepository.save(club);
		
		System.out.println(clubId + "번 모임이 삭제 대기 상태로 변경되었습니다. (7일 후 자동 삭제)");
	}
	
	
	/**
	 * 삭제 대기 중인 모임의 상태를 'ACTIVE'로 되돌리고 삭제 예정 시간을 초기화합니다.
	 * */
	@Transactional
	public void restoreClub(Integer clubId) {
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("복구할 모임을 찾을 수 없습니다."));
		
		// 1. 상태를 다시 활동 중으로 변경
		club.setStatus("ACTIVE");
		
		// 2. 삭제 예정 시간 초기화
		club.setDeletedAt(null);
		
		// 저장 (더티 체킹에 의해 자동 반영)
		clubRepository.save(club);
		
		System.out.println(clubId + "번 모임이 성공적으로 복구되어 다시 리스트에 노출됩니다.");
	}
	
	
	/**
	 * 가입 신청 대기자 목록 조회 (PENDING 상태인 유저들)
	 */
	@Transactional(readOnly = true)
	public List<ClubJoinRequestDTO> getPendingRequests(Integer clubId) {
		// 1. 해당 모임의 멤버들 중 상태가 'PENDING'인 엔티티들만 조회
		List<ClubMemberEntity> pendingEntities = clubMemberRepository.findByClub_ClubIdAndStatusOrderByRoleAsc(clubId, "PENDING");
		
		// 2. 엔티티 리스트를 DTO 리스트로 변환하면서 '답변 내용'을 채워줌
		return pendingEntities.stream().map(entity -> {
			String memberId = entity.getMember().getMemberId();
			
			// 3. club_join_answers 테이블에서 해당 유저의 답변을 찾아옴
			String answer = clubAnswerRepository.findByClubIdAndMemberId(clubId, memberId)
					.map(ClubJoinAnswerEntity::getAnswerText)
					.orElse("답변이 없습니다."); // 만약 답변이 없을 경우를 대비한 기본값
			
			// 4. 화면(HTML)에 뿌려줄 가방(DTO)에 담기
			return ClubJoinRequestDTO.builder()
					.memberId(memberId)
					.memberName(entity.getMember().getName())
					.answerText(answer)
					.appliedAt(entity.getAppliedAt())
					.status(entity.getStatus())
					.build();
		}).collect(Collectors.toList());
	}
	
	/**
	 * 가입 신청 대기자 목록 조회 (페이징 버전)
	 */
	@Transactional(readOnly = true)
	public Page<ClubJoinRequestDTO> getPendingRequestsPaging(Integer clubId, int page) {
		// 1. 10개씩, 신청일시(appliedAt) 내림차순 정렬 설정
		Pageable pageable = PageRequest.of(page, 10, Sort.by("appliedAt").descending());
		
		Page<ClubMemberEntity> pendingPage = clubMemberRepository.findByClub_ClubIdAndStatus(clubId, "PENDING", pageable);
		
		// 3. Page 안의 엔티티들을 DTO로 변환 (기존 로직 그대로 활용)
		return pendingPage.map(entity -> {
			String memberId = entity.getMember().getMemberId();
			
			// 가입 답변 찾아오기
			String answer = clubAnswerRepository.findByClubIdAndMemberId(clubId, memberId)
					.map(ClubJoinAnswerEntity::getAnswerText)
					.orElse("답변이 없습니다.");
			
			return ClubJoinRequestDTO.builder()
					.memberId(memberId)
					.memberName(entity.getMember().getName())
					.answerText(answer)
					.appliedAt(entity.getAppliedAt())
					.status(entity.getStatus())
					.build();
		});
	}
	
	/**
	 * 가입 승인 로직
	 * */
	@Transactional
	public void approveMember(Integer clubId, String memberId) {
		// 1. 신청 내역 조회
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));
		
		// 2. 안전 장치: PENDING 상태인 경우에만 승인 진행
		if (!"PENDING".equals(member.getStatus())) {
			throw new RuntimeException("대기 상태인 회원만 승인할 수 있습니다.");
		}
		
		ClubEntity club = member.getClub();
		
		// 3. 인원 제한 체크 (선택 사항)
		long currentCount = club.getMembers().stream()
				.filter(m -> "ACTIVE".equals(m.getStatus()))
				.count();
		
		// 3. 정원 체크
		if (currentCount >= club.getMaxMember()) {
			throw new RuntimeException("정원이 초과되었습니다.");
		}
		
		// 4. 상태 업데이트
		member.setStatus("ACTIVE");
		member.setJoinedAt(LocalDateTime.now()); // 정식 가입 시간 기록
		
		// 5. 답변 삭제 (내용 가방 비우기)
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		
		// 6. 승인 이력 남기기
		String managerId = "user15";
		leaveHistory(clubId, memberId, managerId, "JOIN_APPROVE", "모임 가입이 승인되었습니다.");
	}
	
	/**
	 * 가입 거절: 관련 답변 데이터와 멤버 신청 내역을 DB에서 완전히 삭제합니다.
	 */
	
	@Transactional
	public void rejectMember(Integer clubId, String memberId) {
		// 1. 답변 삭제
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		
		// 2. 멤버 신청 내역 삭제
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));
		
		// ⭐ [추가] 거절 이력 남기기 (DB에서 삭제하기 전에 데이터를 참조해야 하므로 삭제 직전에 호출!)
		String managerId = "user10";
		leaveHistory(clubId, memberId, managerId, "JOIN_REJECT", "가입 신청이 거절되었습니다.");
		
		clubMemberRepository.delete(member);
	}
	
	/**
	 * 현재 모임에 정식 가입(ACTIVE)된 멤버들을 직급 순서대로 정렬하여 리스트로 반환
	 * */
	public List<ClubMemberDTO> getActiveMembers(Integer clubId) {
	List<ClubMemberEntity> entities = clubMemberRepository.findByClub_ClubIdAndStatusOrderByRoleAsc(clubId, "ACTIVE");
		
		return entities.stream()
				.map(entity -> ClubMemberDTO.builder()
						.cmId(entity.getCmId())
						.memberId(entity.getMember().getMemberId())
						.memberName(entity.getMember().getName())
						.role(entity.getRole())
						.joinedAt(entity.getJoinedAt())
						.build())
				.collect(Collectors.toList());
	}
	
	
	 /**
	  * 모임장 권한으로 특정 멤버의 직급을 변경하며, 모임장 권한 위임 시 본인은 일반 멤버로 강등됩니다.
	 */
	 @Transactional
	 public void updateMemberRole(Integer clubId, String requesterId, String targetId, String newRole) {
		 log.info("[권한 변경 시작] 모임ID: {}, 대상자: {}, 요청자: {}", clubId, targetId, requesterId);
		 
		 ClubMemberEntity requester = findMember(clubId, requesterId);
		 ClubMemberEntity target = findMember(clubId, targetId);
		 String oldRole = target.getRole();
		 
		 // 1. 요청자 권한 체크 (모임장만 가능)
		 if (!"OWNER".equals(requester.getRole())) {
			 throw new RuntimeException("모임장만 권한을 변경할 수 있습니다.");
		 }
		 
		 // 2. 새로운 권한이 "OWNER"(위임)인 경우 특별 처리
		 if ("OWNER".equals(newRole)) {
			 // [핵심] 기존 모임장(나)을 일반 멤버로 강등
			 requester.setRole("MEMBER");
			 log.info("[권한 위임] 기존 모임장({})이 일반 멤버로 강등되었습니다.", requesterId);
		 }
		 
		 // 3. 대상자의 권한 변경 (MEMBER, MANAGER, 또는 위임받은 OWNER)
		 target.setRole(newRole);
		 
		 // 4. 이력 기록 (NFR-06)
		 ClubMemberHistoryEntity history = ClubMemberHistoryEntity.builder()
				 .club(target.getClub())
				 .targetMember(target.getMember())
				 .actorMember(requester.getMember())
				 .actionType("ROLE_CHANGE")
				 .description(oldRole + " -> " + newRole + " 권한 변경")
				 .build();
		 clubMemberHistoryRepository.save(history);
		 
		 log.info("[권한 변경 완료] {}님의 권한이 {}에서 {}로 변경되었습니다.", targetId, oldRole, newRole);
	 }
	
	/**
	 * 관리 권한(OWNER, MANAGER)에 따라 멤버를 제명하며, 본인 제명이나 운영진의 상급자 제명은 차단합니다.
	 */
	@Transactional
	public void kickMember(Integer clubId, String requesterId, String targetId) {
		// 1. 요청자와 대상자 정보 가져오기
		ClubMemberEntity requester = findMember(clubId, requesterId);
		ClubMemberEntity target = findMember(clubId, targetId);
		
		// 2. 본인 제명 방지
		if (requesterId.equals(targetId)) {
			throw new RuntimeException("자기 자신은 제명할 수 없습니다.");
		}
		
		// 3. 권한별 필터링
		if (requester.getRole().equals("MANAGER")) {
			// 운영진은 일반 멤버만 제명 가능
			if (!target.getRole().equals("MEMBER")) {
				throw new RuntimeException("운영진은 운영진이나 모임장을 제명할 수 없습니다.");
			}
		} else if (!requester.getRole().equals("OWNER")) {
			// 일반 멤버가 이 로직을 호출한 경우
			throw new RuntimeException("제명 권한이 없습니다.");
		}
		
		// 4. 제명 처리 (상태 변경 등)
		target.setStatus("BANNED");
		
		// 이력을 남겨야 나중에 재가입 시도 시 'BANNED' 기록을 보고 차단할 수 있습니다.
		leaveHistory(clubId, targetId, requesterId, "BANNED", "운영진에 의한 강제 제명 처리");
	}
	
	/**
	 * [공통] 모임 ID와 멤버 ID로 멤버 엔티티를 안전하게 조회합니다.
	 */
	
	private ClubMemberEntity findMember(Integer clubId, String memberId) {
		return clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("해당 모임의 멤버를 찾을 수 없습니다. (ID: " + memberId + ")"));
	}
	
	/**
	 * [필수] 특정 모임에서 특정 유저의 상세 멤버 정보(권한, 가입일 등)를 DTO로 반환합니다.
	 */
	public ClubMemberDTO getMemberInfo(Integer clubId, String memberId) {
		// 1. 데이터를 가져옵니다.
		ClubMemberEntity entity = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElse(null);
		
		// 2. 데이터가 아예 없을 때만 null을 반환합니다.
		if (entity == null) {
			return null;
		}
		
		// 3. 상태(PENDING, EXIT, ACTIVE 등)가 담긴 DTO를 그대로 반환합니다.
		return new ClubMemberDTO(entity);
	}
	
	/**
	 * 모임 엔티티를 DTO로 변환하며, 특히 '삭제 대기' 상태일 때 남은 시간을 계산하는 핵심 로직을 포함
	 */
	private ClubDTO convertToDTO(ClubEntity entity) {
		ClubDTO dto = ClubDTO.builder()
				.clubId(entity.getClubId())
				.name(entity.getName())
				.description(entity.getDescription())
				.maxMember(entity.getMaxMember())
				.topicId(entity.getTopicId())
				.cityId(entity.getCityId())
				.imageUrl(entity.getImageUrl())
				.status(entity.getStatus())
				.joinQuestion(entity.getJoinQuestion())
				.build();
		
		// 삭제 대기 상태일 때만 남은 시간 계산
		if ("DELETED_PENDING".equals(entity.getStatus()) && entity.getDeletedAt() != null) {
			LocalDateTime expiryDate = entity.getDeletedAt().plusDays(7); // 삭제 예정일
			Duration duration = Duration.between(LocalDateTime.now(), expiryDate);
			
			long days = duration.toDays();
			long hours = duration.toHoursPart();
			
			if (duration.isNegative()) {
				dto.setRemainingTime("곧 삭제 예정");
			} else {
				dto.setRemainingTime(days + "일 " + hours + "시간 남음");
			}
		}
		return dto;
	}
	
	/** 권한 체크 */
	public void checkAuthority(Integer clubId, String memberId, String requiredRole) {
		// 1. 멤버 정보 조회 (기존의 role 컬럼 활용)
		ClubMemberDTO member = getMemberInfo(clubId, memberId);
		
		if (member == null || !"ACTIVE".equals(member.getStatus())) {
			throw new AccessDeniedException("멤버 정보가 없거나 활동 중이 아닙니다.");
		}
		
		// 2. 권한 체크 로직 (문자열 비교)
		String currentRole = member.getRole(); // 기존 컬럼값 (LEADER, MANAGER, MEMBER 등)
		
		if ("OWNER_ONLY".equals(requiredRole)) {
			if (!"OWNER".equals(currentRole)) {
				throw new AccessDeniedException("모임장만 접근 가능합니다.");
			}
		} else if ("MANAGER_UP".equals(requiredRole)) {
			// 운영진 이상 (모임장 또는 운영진)
			if (!"OWNER".equals(currentRole) && !"MANAGER".equals(currentRole)) {
				throw new AccessDeniedException("운영진 이상의 권한이 필요합니다.");
			}
		}
	}
	
	/*제명자 체크? 기능*/
	public void checkJoinEligibility(Integer clubId, String memberId) {
		// 1. 제명자 체크
		if (clubMemberHistoryRepository.existsByClub_ClubIdAndTargetMember_MemberIdAndActionType(clubId, memberId, "BANNED")) {
			throw new RuntimeException("과거 제명된 이력이 있어 가입 신청이 불가능합니다.");
		}
		
		// 2. 쿨타임 체크 (예: 7일)
		clubMemberHistoryRepository.findFirstByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(clubId, memberId)
				.ifPresent(lastHistory -> {
					if (lastHistory.getActionType().equals("EXIT") || lastHistory.getActionType().equals("JOIN_REJECT")) {
						LocalDateTime limit = lastHistory.getCreatedAt().plusDays(365);
						if (LocalDateTime.now().isBefore(limit)) {
							throw new RuntimeException("탈퇴 또는 거절 후 1년이 경과후 재신청이 가능합니다.");
						}
					}
				});
	}
	
	@Transactional
	public void leaveHistory(Integer clubId, String targetId, String actorId, String actionType, String description) {
		ClubEntity club = clubRepository.findById(clubId).orElseThrow();
		MemberEntity target = memberRepository.findById(targetId).orElseThrow();
		MemberEntity actor = memberRepository.findById(actorId).orElseThrow();
		
		ClubMemberHistoryEntity history = ClubMemberHistoryEntity.builder()
				.club(club)
				.targetMember(target)
				.actorMember(actor)
				.actionType(actionType)
				.description(description)
				.createdAt(LocalDateTime.now())
				.build();
		
		clubMemberHistoryRepository.save(history);
	}
}
