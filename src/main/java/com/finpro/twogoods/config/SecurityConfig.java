package com.finpro.twogoods.config;

import com.finpro.twogoods.security.JwtAuthenticationFilter;
import com.finpro.twogoods.security.JwtAuthenticationHandler;
import com.finpro.twogoods.security.OAuth2SuccessHandler;
import com.finpro.twogoods.service.OAuth2UserService;
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
	private final OAuth2UserService oAuth2UserService;
	private final OAuth2SuccessHandler successHandler;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of("*"));
		config.setAllowedMethods(List.of(
				"GET",
				"POST",
				"PUT",
				"PATCH",
				"DELETE",
				"OPTIONS"
		));

		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

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

						// Public
						.requestMatchers(
								"/api/v1/midtrans/**"
										).
						permitAll()

						.requestMatchers(
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/api/v1/auth/**",
								"/oauth2/**",
								"/login/**"
						).permitAll()

						.requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()

						// CUSTOMER
						.requestMatchers("/api/v1/customers/**")
						.hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/buy-now/**")
						.hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/request-cancel")
						.hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/request-return")
						.hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/v1/transactions/me")
						.hasAnyRole("CUSTOMER", "ADMIN")

						// MERCHANT
						.requestMatchers("/api/v1/merchant/**")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/products")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.PUT, "/api/v1/products/**")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.DELETE, "/api/v1/products/**")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/v1/transactions/merchant/**")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/confirm-cancel")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/confirm-return")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/reject-cancel")
						.hasAnyRole("MERCHANT", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/transactions/*/reject-return")
						.hasAnyRole("MERCHANT", "ADMIN")


						// BOTH (merchant & customer)
						.requestMatchers(HttpMethod.PUT, "/api/v1/transactions/*/status")
						.hasAnyRole("MERCHANT", "CUSTOMER", "ADMIN")

						// ADMIN
						.requestMatchers("/api/v1/admin/**")
						.hasRole("ADMIN")

						.anyRequest().authenticated()
				)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jwtAuthenticationHandler.authenticationEntryPoint())
						.accessDeniedHandler(jwtAuthenticationHandler.accessDeniedHandler())
				)
				.oauth2Login(oauth -> oauth
						.userInfoEndpoint(user -> user.userService(oAuth2UserService))
						.successHandler(successHandler)
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
