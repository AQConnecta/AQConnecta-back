package com.aqConnecta.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.aqConnecta.service.DBService;

@Configuration
@Profile("dev")
public class DevConfig {

	@Autowired
	private DBService dbService;
	
	@Value("${instaciaDB}")
	private String value;
	
	@Bean
	boolean instanciaDB() {
		if(value.equals("true")) {
			this.dbService.instanciaDB();
		}
		return false;
	}
}