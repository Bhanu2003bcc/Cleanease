package com.mrer.cleanease.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF for APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/register").permitAll()
                        .requestMatchers("/api/v1/users/health").permitAll()
                        //users
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/role/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/deactivate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/activate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/update-role/**").hasRole("ADMIN")
                        //users
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/id/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/email/**").hasAnyRole("ADMIN", "STAFF")
                        //order
                        .requestMatchers(HttpMethod.POST, "/api/v1/order/**").hasAnyRole("CUSTOMER", "ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/order/my-orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/order/admin/orders/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/order/admin/{orderId}/status/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/order/{orderId}/cancel/**").hasAnyRole("CUSTOMER", "ADMIN", "STAFF")

                        //.requestMatchers(HttpMethod.GET, "/api/v1/order/customer_id/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/order/status/*").hasAnyRole("ADMIN", "STAFF")

                        //Payment
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/*").hasAnyRole("ADMIN", "CUSTOMER")
                        //users
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").hasAnyRole("ADMIN", "CUSTOMER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*").hasAnyRole("ADMIN", "CUSTOMER", "STAFF")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
