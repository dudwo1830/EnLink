package net.datasa.EnLink.community.service;

import net.datasa.EnLink.community.dto.ClubDTO;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 로직 도중 하나라도 실패하면 전체 롤백
public class ClubService {
	
	private final ClubRepository clubRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubMemberHistoryRepository historyRepository;
	
	/**
	 * 새로운 모임 개설
	 * @param clubEntity 화면에서 넘어온 모임 정보
	 * @param leaderId 현재 로그인한 사용자 ID (방장이 됨)
	 * @return 생성된 모임 정보
	 */
	@Transactional
	public ClubEntity createClub(ClubDTO clubDTO, String leaderId) { // 1. 첫 번째 매개변수 타입을 ClubDTO로 변경
		// 2. DTO에 담긴 데이터를 Entity로 복사 (변환 작업)
		ClubEntity entity = new ClubEntity();
		entity.setName(clubDTO.getName());
		entity.setDescription(clubDTO.getDescription());
		entity.setMaxMember(clubDTO.getMaxMember());
		entity.setJoinQuestion(clubDTO.getJoinQuestion());
		
		// 3. 임시 값 설정 (나중에 팀원 코드와 합칠 부분)
		entity.setTopicId(1);
		entity.setCityId(1);
		entity.setStatus("ACTIVE");
		
		// 4. 모임 저장
		ClubEntity savedClub = clubRepository.save(entity);
		
		// 5. 방장 등록 및 이력 저장 로직 실행 (기존 메서드 활용)
		// saveLeaderAndHistory(savedClub.getClubId(), leaderId);
		
		return savedClub;
	}
	
	/**
	 * 전체 모임 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<ClubEntity> getAllClubs() {
		return clubRepository.findAll();
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
	
	public void updateClub(Integer id, ClubDTO clubDTO) {
		// 1. 기존 모임 정보 조회 (없으면 에러 발생)
		ClubEntity club = clubRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다. id=" + id));
		
		// 2. DTO의 내용으로 기존 엔티티 필드 업데이트
		// 수정 가능한 필드만 골라서 업데이트합니다.
		club.setName(clubDTO.getName());
		club.setDescription(clubDTO.getDescription());
		club.setMaxMember(clubDTO.getMaxMember());
		club.setJoinQuestion(clubDTO.getJoinQuestion());
		
		// 3. 만약 이미지 변경 로직이 있다면 여기서 추가 처리
		// club.setImageUrl(clubDTO.getImageUrl());
		
		// 4. 별도의 save() 호출 없이도 @Transactional에 의해 변경 감지(Dirty Checking)로 자동 반영됩니다.
		// 하지만 명시적으로 작성해주셔도 무관합니다.
		// clubRepository.save(club);
	}
}
