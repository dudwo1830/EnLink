package net.datasa.EnLink.community.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.chat.dto.ChatMessageDTO;
import net.datasa.EnLink.community.chat.entity.ChatMessageEntity;
import net.datasa.EnLink.community.chat.repository.ChatMessageRepository;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.repository.ClubMemberRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
	
	private final ChatMessageRepository chatMessageRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final ClubMemberRepository clubMemberRepository;
	
	// 메시지 저장 로직
	public ChatMessageDTO saveMessage(ChatMessageDTO dto, Principal principal) {
		// 1. [보안] 로그인 정보가 없으면 예외 발생
		if (principal == null) {
			throw new AccessDeniedException("로그인이 필요합니다.");
		}
		
		// 2. [핵심] 클라이언트가 보낸 senderId를 무시하고 실제 로그인된 ID를 세팅!
		dto.setSenderId(principal.getName());
		
		// 보안 및 권한 체크: 메시지를 저장하기 전에 이 사용자가 모임 멤버인지 한 번 더 확인
		// 이렇게 하면 조작된 senderId가 들어와도 가입되지 않은 유저라면 여기서 걸러짐
		checkChatAccess(dto.getClubId(), dto.getSenderId());
		
		ClubEntity club = clubRepository.findById(dto.getClubId())
				.orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));
		
		MemberEntity sender = memberRepository.findById(dto.getSenderId())
				.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		
		ChatMessageEntity entity = ChatMessageEntity.builder()
				.club(club)
				.sender(sender)
				.content(dto.getContent())
				.build();
		
		// saveAndFlush를 통해 DB의 sent_at(Auditing) 값이 채워지도록 유도
		ChatMessageEntity saved = chatMessageRepository.saveAndFlush(entity);
		
		// 시간 포맷 처리 (방어 코드 포함)
		LocalDateTime sendTime = (saved.getSentAt() != null) ? saved.getSentAt() : LocalDateTime.now();
		dto.setSentAt(sendTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		dto.setSenderName(sender.getName());
		
		return dto;
	}
	
	@Transactional(readOnly = true)
	public void checkChatAccess(Integer clubId, String memberId) {
		// 1. 해당 유저가 모임에 가입되어 있는지 확인하는 로직 (예시)
		boolean isMember = clubMemberRepository.existsByClub_ClubIdAndMember_MemberId(clubId, memberId);
		
		if (!isMember) {
			// 가입되지 않았다면 예외를 발생시킴
			throw new AccessDeniedException("모임 멤버만 채팅방에 입장할 수 있습니다.");
		}
	}
	
	@Transactional(readOnly = true)
	public Slice<ChatMessageDTO> getChatHistory(Integer clubId, Pageable pageable) {
		// 리포지토리 호출 (이미 최신순 정렬이 메서드명에 포함되어 있음)
		Slice<ChatMessageEntity> entitySlice = chatMessageRepository.findByClub_ClubIdOrderBySentAtDesc(clubId, pageable);
		
		// Entity -> DTO 변환
		List<ChatMessageDTO> dtoList = entitySlice.getContent().stream()
				.map(entity -> ChatMessageDTO.builder()
						.clubId(entity.getClub().getClubId())
						.senderId(entity.getSender().getMemberId())
						.senderName(entity.getSender().getName())
						.content(entity.getContent())
						.sentAt(entity.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) // 시간만 표시
						.isDateDivider(false)
						.build())
				.collect(Collectors.toList());
		
			// [중요] 리스트를 과거순(Asc)으로 정렬하여 정방향으로 화면에 그려지게 함
			// [주의] 날짜가 포함된 포맷으로 정렬해야 올바르게 정렬됨
			// dtoList.sort((d1, d2) -> d1.getSentAt().compareTo(d2.getSentAt()));
		
			// Slice 객체 생성 (기존 Slice 데이터 구조를 유지하며 컨텐츠만 교체)
			return new SliceImpl<>(dtoList, pageable, entitySlice.hasNext());
	}
}
