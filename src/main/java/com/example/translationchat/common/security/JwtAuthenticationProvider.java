package com.example.translationchat.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.common.security.principal.PrincipalDetailsService;
import java.util.Base64;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final PrincipalDetailsService principalDetailsService;

    static final long TOKEN_VALID_TIME = 1000L * 60 * 60 * 24; // 토큰 유효한 시간: 24시간

    @Value("${jwt.secret}")
    private String secretKey;

    // 객체 초기화, secretKey 를 Base64로 인코딩한다.
    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
    }

    private Algorithm getSign() {
        return Algorithm.HMAC512(secretKey);
    }

    // Jwt 토큰 생성
    public String createToken(User user) {
        Date now = new Date();
        return JWT.create()
            .withSubject(user.getEmail())
            .withExpiresAt(new Date(now.getTime() + TOKEN_VALID_TIME))
            .withClaim("id", user.getId())
            .withClaim("email", user.getEmail())
            .sign(getSign());
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        PrincipalDetails principalDetails = (PrincipalDetails) principalDetailsService.loadUserByUsername(this.getUserEmail(token));
        return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
    }

    // 요청값에서 토큰만 추출
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    // 토큰 유효성 검증
    public boolean isValidateToken(String token) {
        // 토큰 만료시간이 현재시간보다 전이면 false
        return !toDecodedJWT(token).getExpiresAt().before(new Date());
    }
    private String getUserEmail(String token) {
        return toDecodedJWT(token).getSubject();
    }
    private DecodedJWT toDecodedJWT(String jwtToken) {
        return JWT.require(getSign())
            .acceptExpiresAt(TOKEN_VALID_TIME)
            .build()
            .verify(jwtToken);
    }


}
