package net.datasa.EnLink.member.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.EnLink.member.dto.request.MemberCreateRequest;
import net.datasa.EnLink.member.dto.request.MemberUpdateRequest;
import net.datasa.EnLink.member.dto.response.MemberDetailResponse;
import net.datasa.EnLink.member.dto.response.MemberUpdateResponse;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.entity.MemberRole;
import net.datasa.EnLink.member.entity.MemberStatus;
import net.datasa.EnLink.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {
	private final BCryptPasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;

	public void create(MemberCreateRequest request) {
		MemberEntity entity = MemberEntity.builder()
				.memberId(request.getMemberId())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.email(request.getEmail())
				.birth(request.getBirth())
				.status(MemberStatus.ACTIVE)
				.role(MemberRole.USER)
				.build();
		memberRepository.save(entity);
	}

	@PreAuthorize("#memberId == principal.memberId")
	public boolean update(MemberUpdateRequest request, String memberId) {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);
		// 기존 비밀번호 확인
		if (entity.getPassword().equals(passwordEncoder.encode(request.getPassword()))) {
			log.debug("\n 기존 비밀번호가 틀렸을 경우");
			return false;
		}
		// 새 비밀번호가 존재할 경우
		if (!request.getNewPassword().isEmpty()) {
			if (request.getNewPassword().equals(request.getRePassword())) {
				entity.updatePassword(passwordEncoder.encode(request.getNewPassword()));
			} else {
				log.debug("\n 새 비밀번호와 재입력이 다른 경우");
				return false;
			}
		}
		entity.updateProfile(request.getName(), request.getEmail(), request.getBirth());
		return true;
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
		entity.updateStatus(MemberStatus.INACTIVE);
	}

	@PreAuthorize("#memberId == principal.memberId")
	public MemberUpdateResponse edit(String memberId) {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);
		return MemberUpdateResponse.builder()
				.memberId(entity.getMemberId())
				.name(entity.getName())
				.email(entity.getEmail())
				.birth(entity.getBirth())
				.build();
	}
}
