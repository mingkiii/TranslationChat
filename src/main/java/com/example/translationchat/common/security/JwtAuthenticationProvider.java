package com.example.translationchat.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.common.security.principal.PrincipalDetailsService;
import java.util.Base64;
import java.util.Date;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

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
    public String createToken(Long id, String email) {
        Date now = new Date();
        return JWT.create()
            .withSubject(email)
            .withExpiresAt(new Date(now.getTime() + TOKEN_VALID_TIME))
            .withClaim("id", id)
            .withClaim("email", email)
            .sign(getSign());
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        PrincipalDetails principalDetails = (PrincipalDetails) principalDetailsService.loadUserByUsername(this.getUserEmail(token));
        return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
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
