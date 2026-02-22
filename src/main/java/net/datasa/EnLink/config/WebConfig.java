package net.datasa.EnLink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.datasa.EnLink.common.locale.PathLocaleInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String rootPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
		
		// 1. ✨ 게시글 미디어 파일 설정
		// URL 패턴을 /images/** 로 하고,
		// 💡 실제 저장 경로: C:/enlink_storage/post/
		String postPath = uploadPath + "post/";
		registry.addResourceHandler("/postImg/**")
				.addResourceLocations("file:///" + postPath);
		
		// 2. 팀원의 갤러리(Gallery) 이미지 설정 (기존 설정 유지)
		registry.addResourceHandler("/galleryImg/**")
				.addResourceLocations("file:///" + uploadPath + "gallery/");
		
		// 3. ✨ 추가: 채팅 미디어 파일 설정
		// URL 패턴을 /chatImg/** 로 하고,
		// 💡 실제 저장 경로: C:/enlink_storage/chat/
		String chatPath = uploadPath + "chat/";
		registry.addResourceHandler("/chatImg/**")
				.addResourceLocations("file:///" + chatPath);
	}

	/**
	 * Locale
	 */
	private final PathLocaleInterceptor pathLocaleInterceptor;

	public WebConfig(PathLocaleInterceptor pathLocaleInterceptor){
		this.pathLocaleInterceptor = pathLocaleInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(pathLocaleInterceptor).addPathPatterns("/ko/**", "/ja/**");
	}
}
