package com.library.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final TokenService tokenService;

    public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter, TokenService tokenService) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> sendUnauthorizedOrForbidden(request.getHeader("Authorization"), response))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/warmup").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Nếu còn giữ DevPasswordController thì KHÔNG permitAll ở bản demo.
                        // Khi cần tạo hash, chỉ bật tạm ở profile dev.
                        .requestMatchers("/api/dev/**").hasRole(RoleConstants.ADMIN)

                        .requestMatchers("/api/auth/me", "/api/auth/change-password", "/api/auth/profile").authenticated()
                        .requestMatchers("/api/reader/**").hasRole("DOC_GIA")

                        .requestMatchers("/api/admin/comments", "/api/admin/comments/**")
                        .hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN)
                        .requestMatchers("/api/admin/**").hasRole(RoleConstants.ADMIN)
                        .requestMatchers("/api/staff/**").hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN)
                        .requestMatchers("/api/reader/**").hasRole(RoleConstants.READER)

                        .requestMatchers("/api/options/", "/api/options/**")
                        .hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN, RoleConstants.READER)

                        // Tra cứu sách: tài khoản đăng nhập nào cũng xem được.
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**",
                                "/api/authors/**",
                                "/api/publishers/**",
                                "/api/books/**",
                                "/api/book-copies/**"
                        ).authenticated()

                        // Quản lý danh mục/sách: thủ thư và admin.
                        .requestMatchers(
                                "/api/categories",
                                "/api/categories/**",
                                "/api/authors",
                                "/api/authors/**",
                                "/api/publishers",
                                "/api/publishers/**",
                                "/api/books",
                                "/api/books/**",
                                "/api/book-copies",
                                "/api/book-copies/**"
                        ).hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN)

                        /*
                         * Endpoint cũ giữ lại để app hiện tại không vỡ.
                         * Sau này có thể chuyển dần sang /api/staff/** hoặc /api/admin/**.
                         */
                        .requestMatchers(
                                "/api/readers",
                                "/api/readers/**",
                                "/api/reader-groups",
                                "/api/reader-groups/**",
                                "/api/membership-plans",
                                "/api/membership-plans/**",
                                "/api/loans",
                                "/api/loans/**",
                                "/api/returns",
                                "/api/returns/**",
                                "/api/payments",
                                "/api/payments/**",
                                "/api/payment-methods",
                                "/api/payment-methods/**"
                        ).hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN)

                        .requestMatchers("/api/reports", "/api/reports/**").hasAnyRole(RoleConstants.LIBRARIAN, RoleConstants.ADMIN)
                        .requestMatchers("/api/activity-logs/**").hasRole(RoleConstants.ADMIN)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:4173",
                "http://127.0.0.1:4173",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "null"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    private void sendUnauthorizedOrForbidden(String authorization, HttpServletResponse response) throws IOException {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                tokenService.parseToken(authorization.substring(7));
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (RuntimeException ignored) {
                // Invalid or expired tokens remain authentication failures.
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
