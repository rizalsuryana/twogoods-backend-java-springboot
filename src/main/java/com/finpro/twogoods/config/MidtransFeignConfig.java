package com.finpro.twogoods.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class MidtransFeignConfig {

	@Value("${midtrans.api-key}")
	private String apiKey;

	@Bean
	public RequestInterceptor midtransInterceptor() {
		return template -> {
			String auth = apiKey + ":";   // <-- IMPORTANT!
			String base64Auth = Base64.getEncoder()
									  .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

			System.out.println(">>>>> USING MIDTRANS SERVER KEY = " + apiKey);
			System.out.println(">>>>> USING AUTH HEADER = Basic " + base64Auth);

			template.header("Authorization", "Basic " + base64Auth);
			template.header("Accept", "application/json");
			template.header("Content-Type", "application/json");
		};
	}
}
