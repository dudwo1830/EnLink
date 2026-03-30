package net.datasa.EnLink.community.service;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.entity.ClubEntity;
import net.datasa.EnLink.community.entity.ClubMemberHistoryEntity;
import net.datasa.EnLink.community.repository.ClubMemberHistoryRepository;
import net.datasa.EnLink.community.repository.ClubRepository;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClubMemberHistoryService {
	private final ClubMemberHistoryRepository clubMemberHistoryRepository;
	private final MemberRepository memberRepository;
	private final ClubRepository clubRepository;
	
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
