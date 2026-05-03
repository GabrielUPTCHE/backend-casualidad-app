package com.casualidad.casualidad_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CasualidadBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasualidadBackendApplication.class, args);
	}

}
