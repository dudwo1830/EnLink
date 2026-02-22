package net.datasa.EnLink.common.locale;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PathLocaleInterceptor implements HandlerInterceptor{
	
	private final LocaleResolver localeResolver;

	public PathLocaleInterceptor(LocaleResolver localeResolver) {
			this.localeResolver = localeResolver;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
		String uri = request.getRequestURI();
		if (uri.startsWith("/ja")) {
			localeResolver.setLocale(request, response, Locale.JAPANESE);
		}else if(uri.startsWith("/ko")){
			localeResolver.setLocale(request, response, Locale.KOREAN);
		}
		return true;
	}
}
