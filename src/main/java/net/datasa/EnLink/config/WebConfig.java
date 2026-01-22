package net.datasa.EnLink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// /images/파일명 으로 요청하면 C:/enlink_uploads/에서 찾아라! 라는 뜻
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:///C:/enlink_uploads/");
	}
}
