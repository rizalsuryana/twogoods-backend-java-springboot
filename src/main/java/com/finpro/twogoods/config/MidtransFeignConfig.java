package com.finpro.twogoods.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class MidtransFeignConfig {

	@Value("${midtrans.server-key}")
	private String serverKey;

	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

	@Bean
	public RequestInterceptor midtransAuthInterceptor() {
		return requestTemplate -> {

			String base64Creds = Base64.getEncoder()
									   .encodeToString((serverKey + ":").getBytes());

			requestTemplate.header("Authorization", "Basic " + base64Creds);
			requestTemplate.header("Content-Type", "application/json");
			requestTemplate.header("Accept", "application/json");
		};
	}
}
