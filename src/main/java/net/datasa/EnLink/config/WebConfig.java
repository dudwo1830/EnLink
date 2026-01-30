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
		// 1. 브라우저가 찾는 주소(/galleryImg/...)와 일치시켜야 합니다.
		registry.addResourceHandler("/galleryImg/**")
				// 2. 실제 파일이 저장된 위치 (C:/enlink_storage/gallery/)
				.addResourceLocations("file:///" + uploadPath + "gallery/");
	}
}
