package net.datasa.EnLink.community.schedule.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberEntity;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.community.schedule.dto.ScheduleDTO;
import net.datasa.EnLink.community.schedule.dto.ScheduleParticipantDTO;
import net.datasa.EnLink.community.schedule.entity.ScheduleEntity;
import net.datasa.EnLink.community.schedule.entity.ScheduleParticipantEntity;
import net.datasa.EnLink.community.schedule.entity.ScheduleParticipantId;
import net.datasa.EnLink.community.schedule.repository.ScheduleParticipantRepository;
import net.datasa.EnLink.community.schedule.repository.ScheduleRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final ScheduleParticipantRepository scheduleParticipantRepository;
	private final ClubMemberRepository clubMemberRepository;
	
	// 1. 일정생성
	public void createSchedule(ScheduleDTO dto, String loginMemberId) {
		// 일정 생성 전, 현재 로그인한 유저가 해당 모임의 운영진인지 확인
		validateAdminRole(dto.getClubId(), loginMemberId);
		
		// 1. 어느 모임의 일정인지 확인
		ClubEntity club = clubRepository.findById(dto.getClubId())
				.orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));
		
		// 2. 작성자(관리자) 정보 확인
		MemberEntity admin = memberRepository.findById(loginMemberId)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
		
		// 3. DTO -> Entity 변환
		ScheduleEntity entity = ScheduleEntity.builder()
				.club(club)
				.admin(admin)
				.title(dto.getTitle())
				.eventDate(dto.getEventDate())
				.location(dto.getLocation())
				.maxCapa(dto.getMaxCapa())
				.build();
		
		// 4. DB 저장
		scheduleRepository.save(entity);
	}
	
	// 2. 일정 목록 조회
	public List<ScheduleDTO> getScheduleList(Integer clubId, String loginMemberId) {
		// 현재 접속한 유저의 권한 확인
		ClubMemberEntity membership = clubMemberRepository
				.findByClub_ClubIdAndMember_MemberId(clubId, loginMemberId)
				.orElse(null);
		
		// 운영진 여부 판단
		boolean isAdmin = membership != null &&
				("OWNER".equals(membership.getRole()) || "MANAGER".equals(membership.getRole()));
		
		// 해당 모임의 일정 리스트 (날짜순 정렬)
		List<ScheduleEntity> entities = scheduleRepository.findByClub_ClubIdOrderByEventDateAsc(clubId);
		
		// Entity 리스트를 DTO 리스트로 변환하면서 각 일정마다 현재 참여자 수를 계산
		return entities.stream().map(entity -> {
			// 기존에 만든 헬퍼 메서드로 기본 정보를 DTO로 변환
			ScheduleDTO dto = convertToDTO(entity, isAdmin);
			
			// [추가] 2. 현재 로그인한 유저가 이 일정에 참여 신청을 했는지 여부 확인
			// 복합키(일정ID, 유저ID)가 DB에 존재하는지 체크합니다.
			boolean isJoined = scheduleParticipantRepository.existsById(
					new ScheduleParticipantId(entity.getSchedId(), loginMemberId)
			);
			dto.setJoined(isJoined); // DTO의 필드명에 따라 setJoined 혹은 setIsJoined 사용
			
			// 이 일정의 'JOIN' 상태인 참여자 수를 DB에서 조회
			long count = scheduleParticipantRepository.countBySchedule_SchedIdAndStatus(entity.getSchedId(),"JOIN");
			// DTO의 새로운 필드에 계산된 값을 세팅
			dto.setCurrentParticipants(count);
			
			return dto;
		}).toList();
	}
	
	// Entity를 DTO로 변환하는 헬퍼 메서드
	private ScheduleDTO convertToDTO(ScheduleEntity entity, boolean isAdmin) {
		return ScheduleDTO.builder()
				.schedId(entity.getSchedId())
				.clubId(entity.getClub().getClubId())
				.title(entity.getTitle())
				.eventDate(entity.getEventDate()) // DTO의 @JsonFormat에 의해 깔끔하게 변환됨
				.location(entity.getLocation())
				.maxCapa(entity.getMaxCapa())
				.adminId(entity.getAdmin().getMemberId())
				// .currentParticipants(...) 나중에 참가 신청 기능 구현 시 추가 예정
				.isAdmin(isAdmin)
				.build();
	}
	
	public void applySchedule(ScheduleParticipantDTO dto) {
		// 1. 일정 정보 먼저 조회 (이 객체 하나로 clubId와 maxCapa를 다 확인합니다)
		ScheduleEntity schedule = scheduleRepository.findById(dto.getSchedId())
				.orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
		
		// 2. 이 유저가 해당 모임의 멤버인지 확인
		clubMemberRepository.findByClub_ClubIdAndMember_MemberId(schedule.getClub().getClubId(), dto.getMemberId())
				.orElseThrow(() -> new RuntimeException("모임 회원만 일정에 참여할 수 있습니다."));
		
		// 3. 이미 신청했는지 체크 (복합키 ID 클래스 사용)
		ScheduleParticipantId id = new ScheduleParticipantId(dto.getSchedId(), dto.getMemberId());
		if (scheduleParticipantRepository.existsById(id)) {
			throw new RuntimeException("이미 참가 신청한 일정입니다.");
		}
		
		// 4. 정원 초과 체크
		// 현재 해당 일정에 'JOIN' 상태인 인원수를 DB에서 직접 셉니다.
		long currentCount = scheduleParticipantRepository.countBySchedule_SchedIdAndStatus(dto.getSchedId(), "JOIN");
		if (schedule.getMaxCapa() != null && currentCount >= schedule.getMaxCapa()) {
			throw new RuntimeException("정원이 초과되어 신청할 수 없습니다.");
		}
		
		// 5. 회원 정보 조회 (저장용)
		MemberEntity member = memberRepository.findById(dto.getMemberId())
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
		
		// 6. 저장용 엔티티 생성 및 최종 저장
		ScheduleParticipantEntity entity = ScheduleParticipantEntity.builder()
				.schedule(schedule)
				.member(member)
				.status("JOIN") // 기본값 설정
				.build();
		
		scheduleParticipantRepository.save(entity);
	}
	
	
	
	// 권한 체크 로직
	private void validateAdminRole(Integer clubId, String memberId) {
		// 1. 이미 작성하신 메서드를 호출하여 멤버 정보를 가져옴
		ClubMemberEntity membership = clubMemberRepository
				.findByClub_ClubIdAndMember_MemberId(clubId, memberId)
				.orElseThrow(() -> new RuntimeException("해당 모임의 멤버가 아닙니다."));
		
		// 2. 가져온 엔티티에서 role을 꺼내 확인
		String role = membership.getRole(); // 'OWNER', 'MANAGER', 'MEMBER'(DB에 ENUM으로 되어있어도 자바에선 String이나 Enum으로 찍힘)
		
		// 3. OWNER나 MANAGER가 아니면 예외를 발생
		if (!"OWNER".equals(role) && !"MANAGER".equals(role)) {
			throw new RuntimeException("운영진(OWNER, MANAGER)만 접근 가능한 기능입니다.");
		}
	}
	
	// 일정 수정
	@Transactional
	public void updateSchedule(ScheduleDTO dto, String loginMemberId) {
		// 수정할 일정 조회
		ScheduleEntity schedule = scheduleRepository.findById(dto.getSchedId())
				.orElseThrow(() -> new EntityNotFoundException("수정할 일정을 찾을 수 없습니다."));
		
		// 운영진 권한 체크
		validateAdminRole(schedule.getClub().getClubId(), loginMemberId);
		
		// 내용 업데이트 (Dirty Checking 활용)
		schedule.setTitle(dto.getTitle());
		schedule.setEventDate(dto.getEventDate());
		schedule.setLocation(dto.getLocation());
		schedule.setMaxCapa(dto.getMaxCapa());
	}
	
	// 일정 삭제
	@Transactional
	public void deleteSchedule(Integer schedId, String loginMemberId) {
		// 삭제할 일정 조회
		ScheduleEntity schedule = scheduleRepository.findById(schedId)
				.orElseThrow(() -> new EntityNotFoundException("삭제할 일정을 찾을 수 없습니다."));
		
		// 운영진 권한 체크
		validateAdminRole(schedule.getClub().getClubId(), loginMemberId);
		
		// 삭제 실행
		scheduleRepository.delete(schedule);
	}
	
	public List<ScheduleParticipantDTO> getParticipantList(Integer schedId) {
		// 1. 해당 일정에 'JOIN' 상태인 데이터만 조회
		List<ScheduleParticipantEntity> entities =
				scheduleParticipantRepository.findBySchedule_SchedIdAndStatus(schedId, "JOIN");
		
		// 2. Entity -> DTO 변환 (멤버 이름 포함)
		return entities.stream().map(entity ->
				ScheduleParticipantDTO.builder()
						.schedId(entity.getSchedule().getSchedId())
						.memberId(entity.getMember().getMemberId())
						.memberName(entity.getMember().getName()) // MemberEntity에 이름 필드가 있다고 가정
						.status(entity.getStatus())
						.build()
		).toList();
	}
	
	// 모임 참여 취소 로직
	public void cancelSchedule(Integer schedId, String loginMemberId) {
		// 1. 해당 일정과 사용자 ID로 구성된 복합키 생성
		ScheduleParticipantId id = new ScheduleParticipantId(schedId, loginMemberId);
		
		// 2. 존재하는지 확인 후 삭제 (모임 회원이면 본인 데이터만 삭제됨)
		if (scheduleParticipantRepository.existsById(id)) {
			scheduleParticipantRepository.deleteById(id);
		} else {
			throw new RuntimeException("참여 정보가 존재하지 않습니다.");
		}
	}
}
