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
		String rootPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
		
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:///" + uploadPath, "classpath:/static/images/");
		
		// 2. 팀원의 갤러리(Gallery) 이미지 설정 (기존 설정 유지)
		registry.addResourceHandler("/galleryImg/**")
				.addResourceLocations("file:///" + uploadPath + "gallery/");
	}
}
