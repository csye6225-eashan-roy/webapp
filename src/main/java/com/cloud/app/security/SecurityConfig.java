package com.cloud.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .httpBasic(withDefaults());

        return http.build();
    }
}
