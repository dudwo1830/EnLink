package net.datasa.EnLink.common.interceptor;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MemberDetailAuthInterceptor implements HandlerInterceptor {

	private final AuthenticatedUserDetailsService authenticatedUserDetailsService;

	MemberDetailAuthInterceptor(AuthenticatedUserDetailsService authenticatedUserDetailsService) {
		this.authenticatedUserDetailsService = authenticatedUserDetailsService;
	}

	// URL이 어떤 컨트롤러 메서드로 향하는지 확정된 상태
	// PathVariable, QueryString을 쉽게 분석할 수 있음
	// DB 조회를 포함한 도메인 로직을 적용 가능
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {

		// 1. 로그인 사용자 정보 가져오기
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// 인증정보가 없거나, 인증되지 않은 사용자 이거나, 익명 사용자일 경우
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals((auth.getPrincipal()))) {
			response.sendRedirect("/member/login");
			return false; // 컨트롤러 접근 차단
		}

		// 2. 인증된 사용자 아이디 알기
		String loginId = auth.getName();
		// 3. 요청된 회원 ID를 경로에서 추출
		String requestURI = request.getRequestURI(); // /member/detail/***
		String targetId = requestURI.substring(requestURI.lastIndexOf("/") + 1);
		// 4. 본인 여부를 확인
		if (!loginId.equals(targetId)) {
			// response 객체가 가진 메서드를 활용하여 지정된 에러를 반환한다
			// 전역 예외처리기를 사용해도 되지만 암묵적으로 컨트롤러-서비스에서만 사용함
			// 고로 이 방법으로 응답을 처리하고 있음
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}

		return true;

	}
}
