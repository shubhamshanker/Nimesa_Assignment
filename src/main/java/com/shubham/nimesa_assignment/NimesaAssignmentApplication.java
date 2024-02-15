package com.shubham.nimesa_assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories
public class NimesaAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(NimesaAssignmentApplication.class, args);
	}

}
