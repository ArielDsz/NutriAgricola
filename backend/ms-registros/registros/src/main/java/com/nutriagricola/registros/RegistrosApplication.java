package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UsersClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = UsersClient.class)
public class RegistrosApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistrosApplication.class, args);
	}

}
