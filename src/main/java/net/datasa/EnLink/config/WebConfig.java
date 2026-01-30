package net.datasa.EnLink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 1. 배경호 님의 모임(Club) 이미지 설정
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:///" + uploadPath);
		
		// 2. 팀원의 갤러리(Gallery) 이미지 설정 (기존 설정 유지)
		registry.addResourceHandler("/galleryImg/**")
				.addResourceLocations("file:///" + uploadPath + "gallery/");
	}
}
