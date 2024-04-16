package com.cloud.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Slf4j
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configuring password encoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        http
                // Apply CSRF protection selectively or disable if necessary
                .csrf(AbstractHttpConfigurer::disable)
                // Configure request authorization
                .authorizeHttpRequests(authz -> authz
                        // a7-start
                        .requestMatchers("/v9/verify-email").permitAll() // Allow unauthenticated access to verify-email
                        // a7-end
                        .requestMatchers("/v9/user/self").authenticated()
                        .anyRequest().permitAll()
                )
                // Configure HTTP Basic authentication
                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException authException) throws IOException {
                                //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                if (authException.getCause() instanceof DataAccessException) {
                                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service temporarily unavailable. Please try again later.");
                                    log.error("Service temporarily unavailable due to {}", authException.getMessage());
                                } else {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                                    log.error("User not authorised due to {}", authException.getMessage());}

                                // No response body will be sent
                            }
                        })
                );
        log.info("Security filter chain configured successfully");
        return http.build();
    }
}
