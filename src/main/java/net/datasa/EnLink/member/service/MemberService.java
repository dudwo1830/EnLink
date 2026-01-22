package net.datasa.EnLink.member.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.dto.request.MemberUpdateRequest;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.entity.MemberStatus;
import net.datasa.EnLink.member.repository.MemberRepository;

@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;

	public void create(MemberCreateRequest request) {
		MemberEntity entity = MemberEntity.builder()
				.memberId(request.getMemberId())
				.password(request.getPassword())
				.name(request.getName())
				.email(request.getEmail())
				.birth(request.getBirth())
				.status(MemberStatus.ACTIVE)
				.build();
		memberRepository.save(entity);
	}

	public void update(MemberUpdateRequest request, String memberId) {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);
		entity.setPassword(request.getPassword());
		entity.setName(request.getName());
		entity.setEmail(request.getEmail());
		entity.setBirth(request.getBirth());
	}

	public MemberDetailResponse read(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);
		return MemberDetailResponse.builder()
				.memberId(entity.getMemberId())
				.name(entity.getName())
				.email(entity.getEmail())
				.birth(entity.getBirth())
				.build();
	}

	public void delete(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);
		entity.setStatus(MemberStatus.INACTIVE);
	}
}
