package com.cloud.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apply CSRF protection selectively or disable if necessary
                .csrf(AbstractHttpConfigurer::disable)
                // Configure request authorization
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/v1/user/self").authenticated()
                        .anyRequest().permitAll()
                )
                // Configure HTTP Basic authentication
                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException authException) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                // No response body will be sent
                            }
                        })
                );

        return http.build();
    }
}
