package com.example.translationchat.common.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final JwtAuthenticationProvider provider;

    public JwtAuthenticationFilter(
        AuthenticationManager authenticationManager,
        JwtAuthenticationProvider provider) {
        super(authenticationManager);
        this.provider = provider;
    }

    // 인증이나 권한이 필요한 주소 요청이 있을 때 필터 동작
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {

        String token = extractTokenFromRequest(request);

        // 토큰을 검증하여 인증 정보를 받는다.
        if (token != null && provider.isValidateToken(token)) {
            // JWT 토큰 서명을 통해서 서명이 정상이면 provider 통해 생성된 Authentication 객체를 가져온다.
            Authentication authentication = provider.getAuthentication(token);

            // 시큐리티 세션에 Authentication 을 저장한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    // 요청값에서 토큰만 추출
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(
            TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
