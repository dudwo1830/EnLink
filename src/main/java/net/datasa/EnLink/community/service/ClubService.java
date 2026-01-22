package net.datasa.EnLink.community.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.dto.ClubJoinRequestDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubJoinAnswerEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import net.datasa.EnLink.community.repository.*;
import net.datasa.EnLink.member.entity.MemberEntity;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // G_001-1: 원자적 처리를 위한 트랜잭션 설정
public class ClubService {
	
	private final ClubRepository clubRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final ClubAnswerRepository clubAnswerRepository;
	private final MemberRepository memberRepository; // 영재님 임시 엔티티용
	
	
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
		
		// 4. 모임(Club) 생성 빌더 구성
		ClubEntity.ClubEntityBuilder clubBuilder = ClubEntity.builder()
				.name(clubDTO.getName())
				.description(clubDTO.getDescription())
				.maxMember(clubDTO.getMaxMember())
				.topicId(clubDTO.getTopicId())
				.cityId(clubDTO.getCityId())
				.joinQuestion(clubDTO.getJoinQuestion())
				.imageUrl(imageUrl);  // ⭐ 핵심
		
		
		// [이미지 처리 로직 추가]
		// 사용자가 파일을 업로드했다면 그 경로로 세팅, 없으면 엔티티의 @Builder.Default 값이 들어감
		if (clubDTO.getUploadFile() != null && !clubDTO.getUploadFile().isEmpty()) {
			// 실제 파일 저장 로직이 구현되면 아래 주석을 풀고 경로를 세팅하세요.
			// String savedFileName = fileService.saveFile(clubDTO.getUploadFile());
			// clubBuilder.imageUrl("/uploads/" + savedFileName);
		}
		
		// 4. 모임 마스터 저장
		ClubEntity club = clubBuilder.build();
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
		// 수정: 모든 모임이 아니라 상태가 "ACTIVE"인 것만 조회
		List<ClubEntity> entities = clubRepository.findByStatus("ACTIVE");
		
		// 이후 DTO 변환 로직은 동일
		return entities.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}
	
	/**
	 * 모임 상세 조회
	 */
	@Transactional(readOnly = true)
	public ClubEntity getClubById(Integer clubId) {
		return clubRepository.findById(clubId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));
	}
	
	/**
	 * 모임 이름 중복 체크
	 */
	public boolean isNameDuplicate(String name) {
		return clubRepository.existsByName(name);
	}
	
	/**
	 * 모임 수정하기
	 *
	 */
	
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
	 * 모임 삭제 요청 (논리 삭제)
	 * 실제 삭제는 ClubScheduler에서 일주일 뒤에 처리함
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
	 * 모임 복구
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
	
	public ClubDTO getClubDetail(Integer clubId) {
		// 1. DB에서 엔티티 조회
		ClubEntity entity = clubRepository.findById(clubId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("해당 모임을 찾을 수 없습니다."));
		
		// 2. 엔티티를 DTO로 변환 (남은 시간 계산 로직이 포함된 convertToDTO 호출)
		return convertToDTO(entity);
	}
	
	/**
	 * 클럽 가입 로직
	 * */
	
	@Transactional
	public void applyToClub(Integer clubId, String memberId, String answerText) {
		ClubEntity club = clubRepository.findById(clubId).orElseThrow();
		MemberEntity member = memberRepository.findById(memberId).orElseThrow();
		
		// 1. 중복 신청 방지
		if (clubMemberRepository.existsByClub_ClubIdAndMember_MemberId(clubId, memberId)) {
			throw new RuntimeException("이미 신청했거나 가입된 상태입니다.");
		}
		
		// 2. club_members (상태 가방) 저장
		ClubMemberEntity clubMember = ClubMemberEntity.builder()
				.club(club)
				.member(member)
				.role("MEMBER")
				.status("PENDING")
				.build();
		clubMemberRepository.save(clubMember);
		
		// 3. club_join_answers (내용 가방) 저장
		ClubJoinAnswerEntity answer = ClubJoinAnswerEntity.builder()
				.clubId(clubId)
				.memberId(memberId)
				.answerText(answerText)
				.build();
		clubAnswerRepository.save(answer);
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
	 * 클럽 가입 신청 취소 로직
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
	 * 가입 신청 대기자 목록 조회 (PENDING 상태인 유저들)
	 */
	@Transactional(readOnly = true)
	public List<ClubJoinRequestDTO> getPendingRequests(Integer clubId) {
		// 1. 해당 모임의 멤버들 중 상태가 'PENDING'인 엔티티들만 조회
		List<ClubMemberEntity> pendingEntities = clubMemberRepository.findByClub_ClubIdAndStatus(clubId, "PENDING");
		
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
	 * 가입 승인: 상태를 PENDING -> ACTIVE로 변경
	 */
	@Transactional
	public void approveMember(Integer clubId, String memberId) {
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));
		
		// 상태 업데이트
		member.setStatus("ACTIVE");
		// 가입 답변은 승인 후 더 이상 필요 없으므로 삭제해도 되고, 이력을 남기려면 유지해도 됩니다.
		// 여기서는 깔끔하게 삭제하는 것으로 처리합니다.
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
	}
	
	/**
	 * 가입 거절: 신청 내역과 답변을 모두 삭제
	 */
	@Transactional
	public void rejectMember(Integer clubId, String memberId) {
		// 1. 답변 삭제
		clubAnswerRepository.deleteByClubIdAndMemberId(clubId, memberId);
		
		// 2. 멤버 신청 내역 삭제
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));
		clubMemberRepository.delete(member);
	}
	
	/**
	 * 모임 탈퇴 처리
	 */
	@Transactional
	public void leaveClub(Integer clubId, String memberId) {
		// 1. 가입된 상태인지 확인
		ClubMemberEntity member = clubMemberRepository.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("가입 정보가 없습니다."));
		
		// 2. 방장(OWNER)은 탈퇴할 수 없도록 방어 로직 (선택 사항)
		if ("OWNER".equals(member.getRole())) {
			throw new RuntimeException("모임장은 탈퇴할 수 없습니다. 모임을 삭제하거나 권한을 위임하세요.");
		}
		
		// 3. 멤버 데이터 삭제
		clubMemberRepository.delete(member);
	}
}