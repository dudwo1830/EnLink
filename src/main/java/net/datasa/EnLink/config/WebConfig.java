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
		// 브라우저에서 /images/** 로 요청이 오면
		// 실제 컴퓨터의 uploadPath 폴더에서 파일을 찾아라!
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:///" + uploadPath);
	}
}
