package com.aqConnecta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AqConnectaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AqConnectaApplication.class, args);
	}

}
