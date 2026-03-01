package net.datasa.EnLink.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		// BCrypt 알고리즘 사용
		return new BCryptPasswordEncoder();
	}

	// 인증 없이 접근 가능한 URL
	private static final String[] PUBLIC_URLS = {
			// 공통 페이지
			"/", "/ko", "/ja", "/error", "/.well-known/**",
			// 정적 리소스
			"/images/**", "/css/**", "/js/**",
			// 로그인, 회원가입
			"/auth/login",
			"/members/signup",
			// api
			"/api/members",
			"/api/location/**",
			"/api/topics",
			"/api/clubs",
			"/api/club/recommend"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.requestMatchers(PUBLIC_URLS).permitAll()
						.anyRequest().authenticated())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(form -> form
						.loginPage("/auth/login")
						.usernameParameter("memberId")
						.passwordParameter("password")
						.loginProcessingUrl("/login")
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
