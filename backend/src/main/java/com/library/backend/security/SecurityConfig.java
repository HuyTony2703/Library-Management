package com.library.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/warmup").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Nếu còn giữ DevPasswordController thì KHÔNG permitAll ở bản demo.
                        // Khi cần tạo hash, chỉ bật tạm ở profile dev.
                        .requestMatchers("/api/dev/**").hasRole("QUAN_TRI_VIEN")

                        .requestMatchers("/api/auth/me").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("QUAN_TRI_VIEN")
                        .requestMatchers("/api/staff/**").hasAnyRole("THU_THU", "QUAN_TRI_VIEN")
                        .requestMatchers("/api/reader/**").hasRole("DOC_GIA")

                        .requestMatchers("/api/options/", "/api/options/**")
                        .hasAnyRole("THU_THU", "QUAN_TRI_VIEN", "DOC_GIA")

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
                        ).hasAnyRole("THU_THU", "QUAN_TRI_VIEN")

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
                        ).hasAnyRole("THU_THU", "QUAN_TRI_VIEN")

                        .requestMatchers("/api/reports", "/api/reports/**").hasAnyRole("THU_THU", "QUAN_TRI_VIEN")
                        .requestMatchers("/api/activity-logs/**").hasRole("QUAN_TRI_VIEN")

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
}
