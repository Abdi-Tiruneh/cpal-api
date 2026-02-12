package com.commercepal.apiservice.config;//package com.commercepal.apiservice.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
/// **
// * Security Configuration for E-Commerce Platform
// *
// * Configured for high-performance API with proper security practices.
// * Public endpoints for product browsing, authenticated endpoints for user actions.
// */
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http
//			.authorizeHttpRequests(auth -> auth
//				// Public endpoints (no authentication required)
//				.requestMatchers(
//					"/api/v1/products/**",
//					"/api/v1/categories/**",
//					"/actuator/health",
//					"/actuator/info"
//				).permitAll()
//				// All other endpoints require authentication
//				.anyRequest().authenticated()
//			)
//			// Disable CSRF for stateless API (use token-based auth instead)
//			.csrf(AbstractHttpConfigurer::disable)
//			// Enable HTTP Basic for API authentication (consider JWT for production)
//			.httpBasic(httpBasic -> {});
//
//		return http.build();
//	}
//}
//
