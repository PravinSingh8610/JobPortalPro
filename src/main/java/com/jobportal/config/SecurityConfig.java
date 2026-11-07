
package com.jobportal.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    // PASSWORD ENCRYPTION
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // LOGIN SUCCESS HANDLER
    // REDIRECT ACCORDING TO USER ROLE
    @Bean
    public AuthenticationSuccessHandler successHandler() {

        return new AuthenticationSuccessHandler() {

            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication)
                    throws IOException, ServletException {

                boolean isAdmin = authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN"));

                boolean isRecruiter = authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_RECRUITER"));

                boolean isCandidate = authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_CANDIDATE"));

                if (isAdmin) {
                    response.sendRedirect(
                            request.getContextPath() + "/admin/dashboard");
                }
                else if (isRecruiter) {
                    response.sendRedirect(
                            request.getContextPath() + "/recruiter/dashboard");
                }
                else if (isCandidate) {
                    response.sendRedirect(
                            request.getContextPath() + "/candidate/dashboard");
                }
                else {
                    response.sendRedirect(
                            request.getContextPath() + "/login?error=true");
                }
            }
        };
    }

    // CUSTOM ACCESS DENIED HANDLER
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {

        return new AccessDeniedHandler() {

            @Override
            public void handle(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    org.springframework.security.access.AccessDeniedException exception)
                    throws IOException, ServletException {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access Denied"
                );
            }
        };
    }

    // SPRING SECURITY CONFIGURATION
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler successHandler,
            AccessDeniedHandler customAccessDeniedHandler)
            throws Exception {

        http

            .authorizeHttpRequests(auth -> auth

                // PUBLIC URLS
                .requestMatchers(
                        "/",
                        "/register",
                        "/login",
                        "/candidate/register",
                        "/recruiter/register",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                ).permitAll()

                // ADMIN URLS
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // CANDIDATE URLS
                .requestMatchers("/candidate/**")
                .hasRole("CANDIDATE")

                // RECRUITER URLS
                .requestMatchers("/recruiter/**")
                .hasRole("RECRUITER")

                // OTHER URLS
                .anyRequest().authenticated()
            )

            // LOGIN CONFIGURATION
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // LOGOUT CONFIGURATION
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // CUSTOM 403 PAGE
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(customAccessDeniedHandler)
            );

        return http.build();
    }
}
