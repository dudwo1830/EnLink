package net.datasa.EnLink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EnLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnLinkApplication.class, args);
	}

}
