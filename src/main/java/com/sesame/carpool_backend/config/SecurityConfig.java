package com.sesame.carpool_backend.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesame.carpool_backend.config.jwt.JwtAuthFilter;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] WHITE_LIST_URI = {
            "/users/seed",
            "/auth/**",
            "/rides/filter",
            "/rides/latest",
            "/rides/generate",
            "/uploads/**",
    };
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(WHITE_LIST_URI)
                        .permitAll()
                        .requestMatchers("/statistics").hasAuthority("ADMIN")
                        .requestMatchers("/users/admin").hasAuthority("ADMIN")
                        .requestMatchers("/cars/**").hasAuthority("DRIVER")
                        .requestMatchers("/rides/driver/**").hasAuthority("DRIVER")
                        .requestMatchers("/ride-requests/passenger/**").hasAuthority("PASSENGER")
                        .requestMatchers("/ride-requests/driver/**").hasAuthority("DRIVER")
                        .anyRequest()
                        .authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ObjectMapper objectMapper = new ObjectMapper();
                            String jsonResponse = objectMapper.writeValueAsString(
                                    new MessageResponse("Access Denied. You do not have the required role.", HttpStatus.UNAUTHORIZED.value())
                            );
                            response.setContentType("application/json");
                            response.getWriter().write(jsonResponse);
                            response.getWriter().flush();
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(HttpServletResponse.SC_OK);
                            ObjectMapper objectMapper = new ObjectMapper();
                            String jsonResponse = objectMapper.writeValueAsString(
                                    new MessageResponse("Logout successfully", HttpStatus.OK.value())
                            );
                            response.setContentType("application/json");
                            response.getWriter().write(jsonResponse);
                            response.getWriter().flush();
                        })
                );
        return http.build();
    }
}
