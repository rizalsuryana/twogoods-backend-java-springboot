package com.finpro.twogoods.config;

import com.finpro.twogoods.security.JwtAuthenticationFilter;
import com.finpro.twogoods.security.JwtAuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session
										   .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
								  )
				.authorizeHttpRequests(auth -> auth

											   //  PUBLIC
											   .requestMatchers(
													   "/swagger-ui/**",
													   "/v3/api-docs/**",
													   "/api/v1/auth/**"
															   ).permitAll()

											   // Products
											   .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()

											   // Customers
											   .requestMatchers("/api/v1/customers/**").hasAnyRole("CUSTOMER", "ADMIN")
											   .requestMatchers(HttpMethod.POST, "/api/v1/transactions/**").hasRole("CUSTOMER")
											   .requestMatchers(HttpMethod.GET, "/api/v1/transactions/me").hasRole("CUSTOMER")

											   // MERCHANT
											   .requestMatchers("/api/v1/merchant/**").hasAnyRole("MERCHANT", "ADMIN")
											   .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("MERCHANT")
											   .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("MERCHANT")
											   .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("MERCHANT")
											   .requestMatchers(HttpMethod.GET, "/api/v1/transactions/merchant").hasRole("MERCHANT")

											   // ADMIN
											   .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

											   // FALLBACK
											   .anyRequest().authenticated()
									  )
				.exceptionHandling(ex -> ex
										   .authenticationEntryPoint(jwtAuthenticationHandler.authenticationEntryPoint())
										   .accessDeniedHandler(jwtAuthenticationHandler.accessDeniedHandler())
								  )
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}


	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
