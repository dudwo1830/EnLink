package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
import net.datasa.EnLink.community.dto.ClubMemberDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubJoinAnswerEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.repository.ClubAnswerRepository;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	private final ClubRepository clubRepository;
	private final ClubAnswerRepository clubAnswerRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final ClubMemberHistoryService clubMemberHistoryService;
	
	/**
	 * 모임 ID로 모임 엔티티를 직접 조회합니다.
	 */
	@Transactional(readOnly = true)
	public ClubEntity getClubById(Integer clubId) {
		return clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
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
	public void updateClub(Integer id, ClubDTO clubDTO, String loginId) {
		checkAuthority(id, loginId, "MANAGER_UP");
		ClubEntity club = getClubById(id);
		
		validateMaxMember(id, clubDTO.getMaxMember());
		
		String oldImageUrl = club.getImageUrl();
		
		if (clubDTO.isDefaultImage()) {
			// [경우 A] 기본 이미지로 변경 시
			club.setImageUrl("/images/default_club.jpg");
			deleteRealFile(oldImageUrl);
		}
		else if (isNewFileUploaded(clubDTO.getUploadFile())) {
			// [경우 B] 새 이미지로 교체 시
			String newImageUrl = storeUploadFile(clubDTO.getUploadFile());
			if (newImageUrl != null) {
				club.setImageUrl(newImageUrl);
				deleteRealFile(oldImageUrl);
			}
		}
		
		club.setName(clubDTO.getName());
		club.setDescription(clubDTO.getDescription());
		club.setMaxMember(clubDTO.getMaxMember());
		club.setJoinQuestion(clubDTO.getJoinQuestion());
	}
	
	/**
	 * 모임 상태를 'DELETED_PENDING'으로 변경하고 삭제 시간을 기록하여 7일 유예 기간을 시작합니다.
	 */
	@Transactional
	public void deleteClub(Integer clubId, String loginId) {
		
		checkAuthority(clubId, loginId, "OWNER_ONLY");
		
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
		
		club.setStatus("DELETED_PENDING");
		clubRepository.save(club);
		club.setDeletedAt(LocalDateTime.now());
		
		clubMemberHistoryService.leaveHistory(clubId, loginId, loginId, "CLUB_DELETE", "모임 삭제 요청");
		log.info("[모임 삭제 요청 완료] 모임ID: {}, 요청자: {}", clubId, loginId);
	}
	
	/**
	 * 삭제 대기 중인 모임의 상태를 'ACTIVE'로 되돌리고 삭제 예정 시간을 초기화합니다.
	 * */
	@Transactional
	public void restoreClub(Integer clubId, String loginId) {
		checkAuthority(clubId, loginId, "OWNER_ONLY");
		ClubEntity club = getClubById(clubId);
		
		// 1. 상태 검증: 오직 삭제 대기 중일 때만 복구 가능
		if (!"DELETED_PENDING".equals(club.getStatus())) {
			throw new BusinessException(ErrorCode.CLUB_NOT_IN_PENDING_STATE);
		}
		
		// 2. 시간 검증: 7일이 지났는지 체크 (선택 사항)
		if (club.getDeletedAt() != null &&
				club.getDeletedAt().plusDays(7).isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND);
		}
		
		// 3. 정책 검증: 복구했을 때 5개가 넘지 않는지 확인 (핵심!)
		long activeCount = clubMemberRepository.countByMember_MemberIdAndRoleAndStatus(loginId, "OWNER", "ACTIVE");
		if (activeCount >= 5) {
			throw new BusinessException(ErrorCode.CLUB_OWN_LIMIT_EXCEEDED);
		}
		
		// 4. 상태 복구
		club.setStatus("ACTIVE");
		club.setDeletedAt(null);
		
		clubMemberHistoryService.leaveHistory(clubId, loginId, loginId, "CLUB_RESTORE", "모임 삭제 취소 및 복구");
		
		log.info("[모임 복구 완료] 모임ID: {}, 요청자: {}", clubId, loginId);
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
			
			// 2. DTO 기본 생성
			ClubJoinRequestDTO dto = ClubJoinRequestDTO.builder()
					.memberId(memberId)
					.memberName(entity.getMember().getName())
					.answerText(answer)
					.appliedAt(entity.getAppliedAt())
					.status(entity.getStatus())
					.build();
			
			clubMemberHistoryRepository.findFirstByClub_ClubIdAndTargetMember_MemberIdOrderByCreatedAtDesc(clubId, memberId)
					.ifPresent(history -> {
						dto.setLastActionType(history.getActionType());
						dto.setLastDescription(history.getDescription());
						dto.setLastActionDate(history.getCreatedAt());
					});
			return dto;
		});
	}
	
	/**
	 * 가입 승인 로직
	 * */
	@Transactional
	public void approveMember(Integer clubId, String memberId, String loginId) {
		
		checkAuthority(clubId, loginId, "MANAGER_UP");
		
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
		
		if (!"PENDING".equals(member.getStatus())) {
			throw new BusinessException(ErrorCode.ALREADY_JOINED_OR_PENDING);
		}
		
		long activeCount = clubMemberRepository.countByMember_MemberIdAndStatus(memberId, "ACTIVE");
		
		if (activeCount >= 5) {
			throw new BusinessException(ErrorCode.TOO_MANY_CLUBS);
		}
		
		ClubEntity club = member.getClub();
		int currentMemberCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		if (currentMemberCount >= club.getMaxMember()) {
			throw new BusinessException(ErrorCode.CLUB_IS_FULL);
		}
		
		member.setStatus("ACTIVE");
		member.setJoinedAt(LocalDateTime.now());
		
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		clubMemberHistoryService.leaveHistory(clubId, memberId,loginId, "JOIN_APPROVE", "모임 가입 승인");
	}
	
	/**
	 * 가입 거절
	 */
	@Transactional
	public void rejectMember(Integer clubId, String memberId, String loginId) {
		
		checkAuthority(clubId, loginId,"MANAGER_UP");
		
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
		
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		clubMemberHistoryService.leaveHistory(clubId, memberId, loginId, "JOIN_REJECT", "가입 신청 거절");
		
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
	  * 권한 변경 (모임장 위임 포함)
	 */
	 @Transactional
	 public void updateMemberRole(Integer clubId, String requesterId, String targetId, String newRole) {
		 checkAuthority(clubId, requesterId, "OWNER_ONLY");
		 
		 ClubMemberEntity requester = findMember(clubId, requesterId);
		 ClubMemberEntity target = findMember(clubId, targetId);
		 String oldRole = target.getRole();
		 
		
		 if ("OWNER".equals(newRole)) {
			 requester.setRole("MEMBER");
			 log.info("[권한 위임] 기존 모임장({})-> 일반 멤버 강등.", requesterId);
		 }
		 
		 target.setRole(newRole);
		 clubMemberHistoryService.leaveHistory(clubId, targetId, requesterId, "ROLE_CHANGE", oldRole + " -> " + newRole + " 권한 변경");
	 }
	
	/**
	 * 멤버 제명 (BANNED)
	 */
	@Transactional
	public void kickMember(Integer clubId, String requesterId, String targetId, String description) {
		// 1. 요청자와 대상자 정보 가져오기
		ClubMemberEntity requester = findMember(clubId, requesterId);
		ClubMemberEntity target = findMember(clubId, targetId);
		
		// 2. 본인 제명 방지
		if (requesterId.equals(targetId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		
		// 3. 권한별 필터링
		if ("MANAGER".equals(requester.getRole())) {
			if (!"MEMBER".equals(target.getRole())) {
				throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
			}
		} else if (!"OWNER".equals(requester.getRole())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
		
		target.setStatus("BANNED");
		clubMemberHistoryService.leaveHistory(clubId, targetId, requesterId, "BANNED", description);
	}
	
	/**
	 * 멤버 조회 공통 메서드
	 */
	private ClubMemberEntity findMember(Integer clubId, String memberId) {
		return clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER));
	}
	
	/**
	 * 특정 모임에서 특정 유저의 상세 멤버 정보(권한, 가입일 등)를 DTO로 반환합니다.
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
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER));
		
		if (!"ACTIVE".equals(member.getStatus())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
		
		String currentRole = member.getRole();
		
		if ("OWNER_ONLY".equals(requiredRole)) {
			if (!"OWNER".equals(currentRole)) {
				throw new BusinessException(ErrorCode.OWNER_ONLY_ACCESS);
			}
		} else if ("MANAGER_UP".equals(requiredRole)) {
			if (!"OWNER".equals(currentRole) && !"MANAGER".equals(currentRole)) {
				throw new BusinessException(ErrorCode.MANAGER_UP_ACCESS);
			}
		}
	}
	
	/**
	 * 상세 페이지용 가입 상태 조회
	 */
	public String getApplyStatus(Integer id, String loginId) {
		if (loginId == null) return null;
		
		return clubMemberRepository.findFirstByClub_ClubIdAndMember_MemberIdOrderByJoinedAtDesc(id, loginId)
				.map(ClubMemberEntity::getStatus)
				.orElse(null);
	}
	
	/**
	 * 모임 수정시 최대 인원 수정 유효성 검사
	 */
	public void validateMaxMember(Integer clubId, int newMaxMember) {
		int currentMemberCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		if (newMaxMember < currentMemberCount) {
			throw new BusinessException(ErrorCode.MAX_MEMBER_LESS_THAN_CURRENT);
		}
	}
	
	/**
	 * 실제 파일 업로드 여부 확인
	 */
	public boolean isNewFileUploaded(MultipartFile file) {
		return file != null && !file.isEmpty();
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
	 * 파일 삭제 로직 (자동 폴더 생성 포함)
	 */
	public void deleteRealFile(String imageUrl) {
		// 1. 기본 이미지는 지우면 안 되니까 방어 로직 추가
		if (imageUrl == null || imageUrl.equals("/images/default_club.jpg")) {
			return;
		}
		
		try {
			// 2. DB 저장 경로(/images/파일명)를 실제 서버 경로(C:/enlink_storage/파일명)로 변환
			String fileName = imageUrl.replace("/images/", "");
			File file = new File(uploadPath + fileName);
			
			// 3. 파일이 존재하면 삭제
			if (file.exists()) {
				if (file.delete()) {
					log.info("기존 파일 삭제 성공: {}", fileName);
				} else {
					log.warn("기존 파일 삭제 실패: {}", fileName);
				}
			}
		} catch (Exception e) {
			log.error("파일 삭제 중 에러 발생: {}", e.getMessage());
		}
	}
}
