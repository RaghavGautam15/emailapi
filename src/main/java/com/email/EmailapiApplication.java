package com.email;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class EmailapiApplication {

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EmailapiApplication.class);
	}


	public static void main(String[] args) throws Exception {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(EmailapiApplication.class);
		builder.headless(false).run(args);

	}
}
