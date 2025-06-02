package com.es2.dem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemApplication.class, args);
	}

}
