package net.datasa.EnLink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class EnLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnLinkApplication.class, args);
	}

}
