package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // G_001-1: 원자적 처리를 위한 트랜잭션 설정
public class ClubService {
	
	private final ClubRepository clubRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final ClubAnswerRepository clubAnswerRepository;
	private final MemberRepository memberRepository; // 영재님 임시 엔티티용
	private final ClubManageService clubManageService;
	
	
	@Value("${file.upload.path:src/main/resources/static/images/}") // 파일 저장 경로 설정
	private String uploadPath;
	
	/**
	 * 모임생성
	 * */
	
	public void createClub(ClubDTO clubDTO, String loginMemberId) {
		// 1. 입력값 검증
		if (clubDTO.getMaxMember() % 10 != 0) {
			throw new IllegalArgumentException("최대 인원은 10단위로 설정해야 합니다.");
		}
		
		// 2. 로그인한 유저 정보 조회
		MemberEntity loginMember = memberRepository.findById(loginMemberId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
		
		// 3. 이미지 처리
		String imageUrl = "/images/default_club.jpg"; // ⭐ URL 기준으로 저장
		
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
				
				imageUrl = "/images/" + savedFileName; // ⭐ 중요
				
			} catch (IOException e) {
				throw new RuntimeException("이미지 저장 실패", e);
			}
		}
		
		// 4. 모임(Club) 저장
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
		
		// 5. 멤버(Member) 등록 (OWNER 권한)
		ClubMemberEntity member = ClubMemberEntity.builder()
				.club(club)
				.member(loginMember)
				.role("OWNER")
				.status("ACTIVE")
				.build();
		clubMemberRepository.save(member);
		
		// 6. 상태 변경 이력 기록 (T-CMH)
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
	 * 활성화 되어있는 모임 목록 조회
	 */
	
	@Transactional(readOnly = true)
	public List<ClubDTO> getClubList() {
		// 1. 상태가 "ACTIVE"인 모임 엔티티 목록 조회
		List<ClubEntity> entities = clubRepository.findByStatus("ACTIVE");
		
		// 2. 각 엔티티를 DTO로 변환하면서 실시간 인원수를 카운트하여 세팅
		return entities.stream()
				.map(entity -> {
					ClubDTO dto = this.convertToDTO(entity);
					
					// [핵심] 해당 모임의 'ACTIVE' 상태인 멤버 수를 DB에서 직접 조회
					int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(entity.getClubId(), "ACTIVE");
					dto.setCurrentMemberCount(currentCount); // DTO에 인원수 저장
					
					return dto;
				})
				.collect(Collectors.toList());
	}
	
	
	/**
	 * 모임 상세 조회 (인원수 포함)
	 */
	@Transactional(readOnly = true)
	public ClubDTO getClubDetail(Integer clubId) {
		// 1. 모임 엔티티 조회
		ClubEntity clubEntity = clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));
		
		// 2. DTO로 변환
		ClubDTO dto = convertToDTO(clubEntity);
		
		// 3. [핵심] 현재 'ACTIVE' 상태인 멤버 수를 카운트하여 DTO에 세팅
		int currentCount = clubMemberRepository.countByClub_ClubIdAndStatus(clubId, "ACTIVE");
		dto.setCurrentMemberCount(currentCount);
		
		return dto;
	}

	
	/**
	 * 모임 이름 중복 체크
	 */
	public boolean isNameDuplicate(String name) {
		return clubRepository.existsByName(name);
	}
	
	
	/**
	 * 클럽 가입 신청
	 * */
	public void applyToClub(Integer clubId, String memberId, String answerText) {
		ClubEntity club = clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));
		MemberEntity member = memberRepository.findById(memberId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
		
		// 1. 기존 신청/가입 기록 확인
		Optional<ClubMemberEntity> existingMember = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId);
		
		if (existingMember.isPresent()) {
			ClubMemberEntity clubMember = existingMember.get();
			String status = clubMember.getStatus();
			
			// 상태별 분기 처리
			if ("ACTIVE".equals(status)) {
				throw new RuntimeException("이미 가입된 멤버입니다.");
			} else if ("PENDING".equals(status)) {
				throw new RuntimeException("이미 가입 신청 중입니다.");
			} else if ("BANNED".equals(status)) {
				throw new RuntimeException("제명된 회원은 재가입이 불가능합니다.");
			}
			
			// ⭐ EXIT(탈퇴) 상태일 경우: 기존 데이터를 PENDING으로 재활용
			clubMember.setStatus("PENDING");
			clubMember.setRole("MEMBER"); // 등급 초기화
			// JPA의 Dirty Checking으로 인해 별도의 save 호출 없이도 트랜잭션 종료 시 업데이트됩니다.
			
		} else {
			// 2. 기록이 전혀 없는 신규 신청일 경우: 새 객체 생성
			ClubMemberEntity newMember = ClubMemberEntity.builder()
					.club(club)
					.member(member)
					.role("MEMBER")
					.status("PENDING")
					.build();
			clubMemberRepository.save(newMember);
		}
		
		// 3. 가입 답변 저장 (기존 답변이 있을 수 있으므로 삭제 후 재저장하거나 업데이트 처리)
		// 기존 답변 삭제 (재신청 시 새로운 답변으로 교체하기 위함)
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		
		ClubJoinAnswerEntity answer = ClubJoinAnswerEntity.builder()
				.clubId(clubId)
				.memberId(memberId)
				.answerText(answerText)
				.build();
		clubAnswerRepository.save(answer);
	}
	
	/**
	 * 클럽 가입 신청 취소
	 * */
	@Transactional
	public void cancelApplication(Integer clubId, String memberId) {
		// 1. 신청 내역이 있는지 확인
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));
		
		// 2. 답변 삭제 (club_join_answers)
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		
		// 3. 멤버 신청 삭제 (club_members)
		clubMemberRepository.delete(member);
	}
	
	
	/**
	 * 모임 탈퇴 처리
	 */
	@Transactional
	public void leaveClub(Integer clubId, String memberId) {
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("가입 정보가 없습니다."));
		
		if ("OWNER".equals(member.getRole())) {
			throw new RuntimeException("모임장은 탈퇴할 수 없습니다.");
		}
		
		// 1. 삭제 대신 상태 변경 (나중에 쿨타임 체크를 위해)
		member.setStatus("EXIT");
		
		member.setRole("MEMBER");
		
		// 2. 이력 남기기
		clubManageService.leaveHistory(clubId, memberId, memberId, "EXIT", "자진 탈퇴");
		
		log.info("탈퇴 처리 완료 (상태: EXIT)");
	}
	
	/**
	 * entity -> DTO 변환
	 * */
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
	
	/**
	 * 유저의 신청 상태 조회
	 */
	@Transactional(readOnly = true)
	public String getApplicationStatus(Integer clubId, String memberId) {
		return clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.map(ClubMemberEntity::getStatus)
				.orElse(null);
	}
	
	
	/**
	 * 상세페이지 가입된 회원정보 출력
	 * */
	public List<ClubMemberDTO> getActiveMembers(Integer clubId) {
		List<ClubMemberEntity> entities = clubMemberRepository.findByClub_ClubIdAndStatusOrderByRoleAsc(clubId, "ACTIVE");
		
		return entities.stream()
				.map(entity -> ClubMemberDTO.builder()
						.memberId(entity.getMember().getMemberId())
						.memberName(entity.getMember().getName()) // ⭐ 이름 추가
						.role(entity.getRole())
						.build())
				// ⭐ 직급 순서 정렬 (OWNER -> MANAGER -> MEMBER 순)
				.sorted(Comparator.comparingInt(m -> {
					if (m.getRole().equals("OWNER")) return 1;
					if (m.getRole().equals("MANAGER")) return 2;
					return 3;
				}))
				.toList();
	}
	
	/**
	 * 특정 모임에서 해당 유저의 역할(Role)을 가져옵니다.
	 */
	public String getMemberRole(Integer clubId, String memberId) {
		// 1. 모임 ID와 멤버 ID로 가입 정보를 찾습니다.
		return clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.map(ClubMemberEntity::getRole) // 정보가 있으면 Role(OWNER 등)을 꺼냅니다.
				.orElse(null);                  // 정보가 없으면(미가입자) null을 반환합니다.
	}
}