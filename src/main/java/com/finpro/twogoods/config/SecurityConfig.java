package com.finpro.twogoods.config;

import com.finpro.twogoods.security.JwtAuthenticationFilter;
import com.finpro.twogoods.security.JwtAuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationHandler jwtAuthenticationHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
										   .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
								  )
				.authorizeHttpRequests(authorize -> authorize
						 .requestMatchers(
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/api/v1/auth/**",
								"/api/v1/users"
						).permitAll()

						.requestMatchers("/api/**").hasRole("ADMIN")
						 
						//  PUBLIC: GET products
						.requestMatchers("GET", "/api/v1/products/**").permitAll()

						//  MERCHANT ONLY
						.requestMatchers("POST", "/api/v1/products").hasRole("MERCHANT")
						.requestMatchers("PUT", "/api/v1/products/**").hasRole("MERCHANT")
						.requestMatchers("DELETE", "/api/v1/products/**").hasRole("MERCHANT")
						.requestMatchers("POST", "/api/v1/products/*/upload-multi-images").hasRole("MERCHANT")
						.requestMatchers("POST", "/api/v1/products/suggest-price").hasRole("MERCHANT")
						.requestMatchers("/api/v1/merchant/**").hasRole("MERCHANT")
						//  ADMIN ONLY
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
						//  CUSTOMER ONLY
						.requestMatchers("/api/v1/customers/**").hasRole("CUSTOMER")

						.anyRequest().authenticated()
				)

				.exceptionHandling(ex -> ex
										   .authenticationEntryPoint(jwtAuthenticationHandler.authenticationEntryPoint())
										   .accessDeniedHandler(jwtAuthenticationHandler.accessDeniedHandler())
								  )
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
