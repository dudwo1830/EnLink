package net.datasa.EnLink.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.entity.MemberStatus;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

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

	// public void update(MemberUpdateRequest request, String memberId) {
	// MemberEntity entity = memberRepository.findById(memberId).orElse(null);
	// entity.setPassword(request.getPassword());
	// entity.setName(request.getName());
	// entity.setEmail(request.getEmail());
	// entity.setBirth(request.getBirth());
	// }

	// public MemberDetailResponse read(String memberId) {
	// MemberEntity entity = memberRepository.findById(memberId).orElse(null);
	// return MemberDetailResponse.builder()
	// .memberId(entity.getMemberId())
	// .name(entity.getName())
	// .email(entity.getEmail())
	// .birth(entity.getBirth())
	// .build();
	// }

	public void delete(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		entity.setStatus(MemberStatus.INACTIVE);
	}
}
