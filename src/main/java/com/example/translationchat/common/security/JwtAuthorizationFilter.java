package com.example.translationchat.common.security;

import static com.example.translationchat.common.exception.ErrorCode.LOGIN_REQUIRED;

import com.example.translationchat.common.exception.CustomException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider provider;

    public JwtAuthorizationFilter(JwtAuthenticationProvider provider) {
        this.provider = provider;
    }

    // 인증이나 권한이 필요한 주소 요청이 있을 때 필터 동작
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        if (requestURI.equals("/user/signup") || requestURI.equals("/user/login")) {
            // 로그인과 회원 가입 엔드포인트는 토큰 검증 없이 통과시키기
            chain.doFilter(request, response);
            return;
        }

        String token = provider.extractTokenFromRequest(request);

        // 토큰을 검증하여 인증 정보를 받는다.
        if (token != null && provider.isValidateToken(token)) {
            // JWT 토큰 서명을 통해서 서명이 정상이면 provider 통해 생성된 Authentication 객체를 가져온다.
            Authentication authentication = provider.getAuthentication(token);

            // 시큐리티 세션에 Authentication 을 저장한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new CustomException(LOGIN_REQUIRED);
        }

        chain.doFilter(request, response);
    }
}
