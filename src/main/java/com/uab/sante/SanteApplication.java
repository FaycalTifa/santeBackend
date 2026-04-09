package com.uab.sante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SanteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SanteApplication.class, args);
	}


}
