package com.casualidad.casualidad_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.net.URL;
import java.util.Scanner;


@SpringBootApplication
@EnableScheduling
public class CasualidadBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasualidadBackendApplication.class, args);
	}

	@PostConstruct
	public void imprimirIpDeSalida() {
		try (Scanner s = new Scanner(new URL("https://ifconfig.me").openStream(), "UTF-8").useDelimiter("\\A")) {
			System.out.println("\n=======================================================");
			System.out.println("🚀 IP PUBLICA DE AWS APP RUNNER: " + s.next());
			System.out.println("=======================================================\n");
		} catch (Exception e) {
			System.out.println("⚠️ No se pudo obtener la IP pública: " + e.getMessage());
		}
	}

}
