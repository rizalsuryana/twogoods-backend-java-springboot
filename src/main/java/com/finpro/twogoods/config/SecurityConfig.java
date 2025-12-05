package com.finpro.twogoods.config;

import com.finpro.twogoods.security.JwtAuthenticationFilter;
import com.finpro.twogoods.security.JwtAuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter  jwtAuthenticationFilter;
	private final JwtAuthenticationHandler jwtAuthenticationHandler;

	@Bean
	public SecurityFilterChain securityFilterChain (HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
			throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
		    .cors(cors -> cors.configurationSource(corsConfigurationSource))
		    .httpBasic(AbstractHttpConfigurer::disable)
		    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		    .headers(headers -> headers.contentSecurityPolicy(
				                               csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
		                               .xssProtection(
				                               xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.DISABLED)))
		    .authorizeHttpRequests(auth -> auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
		                                       .permitAll()
		                                       .anyRequest()
		                                       .permitAll())
		    .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationHandler.authenticationEntryPoint())
		                               .accessDeniedHandler(jwtAuthenticationHandler.accessDeniedHandler()))
		    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}