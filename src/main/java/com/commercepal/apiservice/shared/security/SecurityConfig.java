
package com.commercepal.apiservice.shared.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityAuditService securityAuditService;

    private final long corsMaxAge = 3600;
    private final int bcryptStrength = 12;

    // ------------------------------------------------------------------------
    // API SECURITY FILTER CHAIN
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // STAFF SECURITY FILTER CHAIN
    // ------------------------------------------------------------------------
    @Bean
    @Order(0)
    public SecurityFilterChain staffSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/staff/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        // Explicitly permit auth endpoints, but we can also lock this down further if
                        // needed
                        .requestMatchers("/api/v1/staff/auth/**").permitAll()

                        // Require authentication for other staff endpoints (future proofing)
                        .requestMatchers("/api/v1/staff/**").authenticated())

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(
                                csp -> csp.policyDirectives(SecurityConfigConstants.CONTENT_SECURITY_POLICY))
                        .addHeaderWriter(new XXssProtectionHeaderWriter()))

                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtTokenProvider,
                                userDetailsService,
                                securityAuditService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ------------------------------------------------------------------------
    // API SECURITY FILTER CHAIN
    // ------------------------------------------------------------------------
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll())

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(
                                csp -> csp.policyDirectives(SecurityConfigConstants.CONTENT_SECURITY_POLICY))
                        .addHeaderWriter(new XXssProtectionHeaderWriter()))

                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtTokenProvider,
                                userDetailsService,
                                securityAuditService),
                        UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutUrl(SecurityConfigConstants.LOGOUT_URL)
                        .addLogoutHandler(
                                new EnhancedLogoutHandler(jwtTokenProvider, securityAuditService))
                        .logoutSuccessHandler((request, response, auth) -> {
                            response.setStatus(200);
                            response.setContentType(SecurityConfigConstants.HEADER_CONTENT_TYPE_JSON);
                            response.getWriter()
                                    .write(SecurityConfigConstants.LOGOUT_SUCCESS_MESSAGE);
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true));

        return http.build();
    }

    // ------------------------------------------------------------------------
    // ACTUATOR SECURITY FILTER CHAIN
    // ------------------------------------------------------------------------
    @Bean
    @Order(2)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher(SecurityConfigConstants.ACTUATOR_SECURITY_MATCHER)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .httpBasic(httpBasic -> httpBasic.realmName(SecurityConfigConstants.ACTUATOR_REALM))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // ------------------------------------------------------------------------
    // GLOBAL FALLBACK FILTER CHAIN (CORS FOR 404s)
    // ------------------------------------------------------------------------
    @Bean
    @Order(99)
    public SecurityFilterChain fallbackSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }

    // ------------------------------------------------------------------------
    // CORS CONFIGURATION
    // ------------------------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://196.188.172.179:3004",
                "https://196.188.172.179:3004",
                "http://localhost:3004",
                "https://localhost:3004"));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(false);

        // If you ever use cookies instead, change to:
        // configuration.setAllowCredentials(true);

        configuration.setMaxAge(corsMaxAge);

        configuration.setExposedHeaders(List.of(
                SecurityConfigConstants.CORS_EXPOSED_HEADERS));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ------------------------------------------------------------------------
    // AUTHENTICATION MANAGER
    // ------------------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ------------------------------------------------------------------------
    // PASSWORD ENCODER
    // ------------------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
