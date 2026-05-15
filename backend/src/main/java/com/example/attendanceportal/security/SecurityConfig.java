package com.example.attendanceportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ================= PUBLIC APIs (FIRST) =================
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/otp/**",
                                "/api/student/send-otp",
                                "/api/student/verify-otp",
                                "/api/admin/login",
                                "/h2-console/**",
                                "/",
                                "/error"
                        ).permitAll()

                        // ================= ADMIN APIs =================
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // ================= STUDENT APIs =================
                        .requestMatchers(
                                "/api/attendance/student/**",
                                "/api/student/attendance-summary"
                        ).hasAuthority("ROLE_STUDENT")

                        // ================= TEACHER APIs =================
                        .requestMatchers(
                                "/api/student/**",
                                "/api/student/all",
                                "/api/teacher/**"
                        ).hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")

                        // ================= REPORT APIs =================
                        .requestMatchers(
                                "/api/attendance/report/**"
                        ).hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")

                        // ================= EXPORT APIs =================
                        .requestMatchers(
                                "/api/attendance/export/**"
                        ).hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")

                        // ================= ATTENDANCE APIs =================
                        .requestMatchers(
                                "/api/attendance/init-today",
                                "/api/attendance/mark",
                                "/api/attendance/summary"
                        ).hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}