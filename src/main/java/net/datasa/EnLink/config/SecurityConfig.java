package net.datasa.EnLink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		// BCrypt 알고리즘 사용
		return new BCryptPasswordEncoder();
	}

	// 인증 없이 접근 가능한 URL
	private static final String[] PUBLIC_URLS = {
			// 메인 페이지
			"/"
			// 에러 페이지
			, "/error"
			// 정적 리소스
			, "/images/**", "/css/**", "/js/**"
			// 사용자 정의
			// 회원
			, "members/signup"
	};

	/**
	 * 프로젝트 초기 세팅
	 * 모두 허용
	 * 
	 * @param http
	 * @return
	 * @throws Exception
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_URLS)
						.permitAll()
						.anyRequest()
						.authenticated())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(form -> form
						.loginPage("/login")
						.usernameParameter("memberId")
						.passwordParameter("password")
						.loginProcessingUrl("/auth/login")
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/auth/logout")
						.logoutSuccessUrl("/"));
		http
				.cors(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}
}
