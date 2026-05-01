package com.be_senior.Haru_Coding_backend;

import com.be_senior.Haru_Coding_backend.global.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class HaruCodingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HaruCodingBackendApplication.class, args);
	}

}
