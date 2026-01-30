package net.datasa.EnLink.common.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.member.entity.MemberRole;
import net.datasa.EnLink.member.entity.MemberStatus;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetails implements UserDetails {
	private String memberId;
	private String password;
	private MemberRole role;
	private MemberStatus status;

	// 권한 목록 반환
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.memberId;
	}

	public String getMemberId() {
		return this.memberId;
	}

	public MemberStatus getStatus() {
		return this.status;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return this.status == MemberStatus.ACTIVE;
	}
}
