package net.datasa.EnLink.common.interceptor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
		MemberEntity entity = memberRepository.findById(memberId).orElse(null);

		return AuthenticatedUser.builder()
				.memberId(entity.getMemberId())
				.password(entity.getPassword())
				.role(entity.getRole())
				.status(entity.getStatus())
				.build();
	}
}
