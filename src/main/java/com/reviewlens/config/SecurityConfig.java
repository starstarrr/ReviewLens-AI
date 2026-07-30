package com.reviewlens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String DEFAULT_FRONTEND_URL = "http://localhost:5173";

    private final String frontendUrl;

    public SecurityConfig(
            @Value("${reviewlens.frontend-url:http://localhost:5173}") String frontendUrl) {

        this.frontendUrl = removeTrailingSlash(frontendUrl);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/error",
                                "/actuator/health",
                                "/actuator/info",
                                "/oauth2/**",
                                "/login/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED)))
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl(
                                frontendUrl,
                                true)
                        .failureUrl(
                                frontendUrl + "/?oauthError=true"));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(frontendUrl));

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"));

        configuration.setAllowedHeaders(
                List.of("*"));

        configuration.setExposedHeaders(
                List.of("Location"));

        configuration.setAllowCredentials(
                true);

        configuration.setMaxAge(
                3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration);

        return source;
    }

    private String removeTrailingSlash(
            String value) {

        if (value == null || value.isBlank()) {
            return DEFAULT_FRONTEND_URL;
        }

        String normalizedValue = value.trim();

        while (normalizedValue.endsWith("/")) {
            normalizedValue = normalizedValue.substring(
                    0,
                    normalizedValue.length() - 1);
        }

        return normalizedValue;
    }
}