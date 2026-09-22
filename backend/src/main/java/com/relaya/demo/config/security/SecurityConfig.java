package com.relaya.demo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final RateLimitingFilter rateLimitingFilter;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthFilter,
			RateLimitingFilter rateLimitingFilter
	) {
		this.jwtAuthFilter = Objects.requireNonNull(jwtAuthFilter, "jwtAuthFilter must not be null");
		this.rateLimitingFilter = Objects.requireNonNull(rateLimitingFilter, "rateLimitingFilter must not be null");
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/health", "/api/v1/health", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
						.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
						.requestMatchers(
								"/api/v1/auth/sse-token",
								"/api/v1/auth/logout",
								"/api/v1/intakes/**",
								"/api/v1/drafts/**",
								"/api/v1/approvals/**"
						).hasAnyAuthority("ROLE_REVIEWER", "ROLE_ADMIN")
						.anyRequest().authenticated()
				)
				.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(
			RelayaUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder
	) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}