package com.brokerage.orderapp.config;

import com.brokerage.orderapp.repository.AppUserRepository;
import com.brokerage.orderapp.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

/**
 *  Authorization Summary:
 *  --------------------------------------------------------------------------
 *  | HTTP Method | Endpoint                | Roles                        |
 *  |-------------|-------------------------|------------------------------|
 *  | POST        | /api/auth/login         | Open to All                  |
 *  | POST        | /api/orders             | ADMIN, CUSTOMER              |
 *  | GET         | /api/orders             | ADMIN, CUSTOMER              |
 *  | DELETE      | /api/orders/{orderId}   | ADMIN, CUSTOMER              |
 *  | GET         | /api/assets             | ADMIN, CUSTOMER              |
 *  | GET         | /api/assets/{assetName} | ADMIN, CUSTOMER              |
 *  | *           | /api/admin/**           | ADMIN                        |
 *  | *           | /api/** (others)        | ADMIN, CUSTOMER              |
 *  --------------------------------------------------------------------------
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Authentication endpoint: open to all
                        .requestMatchers("/api/auth/login").permitAll()

                        // Management endpoints: ADMIN only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Order CRUD endpoints: ADMIN or CUSTOMER
                        .requestMatchers(POST,   "/api/orders").hasAnyRole("ADMIN","CUSTOMER")
                        .requestMatchers(GET,    "/api/orders").hasAnyRole("ADMIN","CUSTOMER")
                        .requestMatchers(DELETE, "/api/orders/*").hasAnyRole("ADMIN","CUSTOMER")

                        // Asset CRUD endpoints: ADMIN or CUSTOMER
                        .requestMatchers(GET,    "/api/assets").hasAnyRole("ADMIN","CUSTOMER")
                        .requestMatchers(GET,    "/api/assets/*").hasAnyRole("ADMIN","CUSTOMER")

                        // Other API endpoints: ADMIN or CUSTOMER
                        .requestMatchers("/api/**").hasAnyRole("ADMIN","CUSTOMER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().split(","))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}