package com.library.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {
    @Mock TokenService tokenService;
    @Mock AuthenticatedPrincipalService principalService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;
    private TokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() { filter = new TokenAuthenticationFilter(tokenService, principalService); }

    @Test
    void forceChangeAccountCannotReachBusinessEndpoint() throws Exception {
        prepare("/api/readers", true);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
        assertThat(body.toString()).contains("PASSWORD_CHANGE_REQUIRED").doesNotContain("token-value");
    }

    @Test
    void forceChangeAccountCanReachChangePasswordEndpoint() throws Exception {
        prepare("/api/auth/change-password", true);
        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    private void prepare(String path, boolean mustChange) {
        AuthUser tokenUser = new AuthUser("TK1", "reader", "VT", RoleConstants.READER,
                "DG1", null, "Reader", 2, false);
        AuthUser refreshed = new AuthUser("TK1", "reader", "VT", RoleConstants.READER,
                "DG1", null, "Reader", 2, mustChange);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-value");
        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("POST");
        when(tokenService.parseToken("token-value")).thenReturn(tokenUser);
        when(principalService.refresh(tokenUser)).thenReturn(refreshed);
    }
}
