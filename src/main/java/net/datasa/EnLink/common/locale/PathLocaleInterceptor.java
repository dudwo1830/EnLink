package net.datasa.EnLink.common.locale;

import java.io.IOException;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PathLocaleInterceptor implements HandlerInterceptor {

    private final LocaleResolver localeResolver;

    public PathLocaleInterceptor(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        Locale locale = null;
        String newUri = uri;

        if (uri.startsWith("/ko")) {
            locale = Locale.KOREAN;
            newUri = uri.substring(3); // /ko 제거
        } else if (uri.startsWith("/ja")) {
            locale = Locale.JAPANESE;
            newUri = uri.substring(3); // /ja 제거
        } else {
            locale = request.getLocale(); // Accept-Language 사용
        }

        localeResolver.setLocale(request, response, locale);
        log.info("Locale set: {}, uri: {}", locale, uri);

        if (!newUri.equals(uri)) {
            // /ko, /ja 제거한 경로로 포워드
            request.getRequestDispatcher(newUri).forward(request, response);
            return false; // 포워드 후 나머지 체인 실행 안 함
        }
        return true;
    }
}
