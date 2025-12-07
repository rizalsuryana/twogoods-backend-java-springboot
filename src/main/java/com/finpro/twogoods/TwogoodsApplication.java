package com.finpro.twogoods;

import com.finpro.twogoods.utils.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients

public class TwogoodsApplication {

	public static void main(String[] args) {
		EnvLoader.load("./.env");
		SpringApplication.run(TwogoodsApplication.class, args);
	}

}
