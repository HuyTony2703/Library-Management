package com.library.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthenticatedPrincipalService authenticatedPrincipalService;

    public TokenAuthenticationFilter(
            TokenService tokenService,
            AuthenticatedPrincipalService authenticatedPrincipalService
    ) {
        this.tokenService = tokenService;
        this.authenticatedPrincipalService = authenticatedPrincipalService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                AuthUser tokenUser = tokenService.parseToken(token);
                AuthUser user = authenticatedPrincipalService.refresh(tokenUser);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority(user.getRoleAuthority()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (user.isMustChangePassword() && !isForceChangeAllowed(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"errorCode\":\"PASSWORD_CHANGE_REQUIRED\",\"message\":\"Bạn phải đổi mật khẩu trước khi tiếp tục\"}");
                    return;
                }
            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isForceChangeAllowed(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return "/api/auth/me".equals(path) || "/api/auth/change-password".equals(path);
    }
}
