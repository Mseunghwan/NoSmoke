package org.example.nosmoke.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.token.TokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 생성 및 검증을 담당하는 클래스
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final long accessTokenValidTime = 30 * 60 * 1000L; // 토큰 유효시간 30분
    private final long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L; // 리프레시 토큰 2주 유지

    // UserDetailsService 주입 --> UserDetails? SpringSecurity 에서 사용자의 정보를 담는 인터페이스
    // 사용자 정보 불러오기 위해 구현해야하는 인터페이스
    // 이 UserDetailService에 Spring이 내가 작성한 CustomUsreDetailService 주입해줌
    private final UserDetailsService userDetailsService;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //JWT 토큰 생성 -> Access + Refresh 토큰 둘 다 발급
    public TokenDto createTokenDto(String userPk, String email) {
        Claims claims = Jwts.claims().setSubject(userPk); // JWT payload에 저장되는 정보단위
        claims.put("email", email); // 정보는 key / value 단위로 저장된다, 즉 "email" = emai 값
        Date now = new Date();

        // 1. Access Token 생성
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 2. Refresh Token 생성
        String refrestToken = Jwts.builder()
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refrestToken)
                .accessTokenExpiresIn(now.getTime() + accessTokenValidTime)
                .build();
    }

    // 토큰 남은 유효시간(TTL) 계산 - 로그아웃 시 BlackList 저장하기 위함
    public long getExpiration(String accessToken) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(accessToken).getBody().getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }

    // 1. 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        // 토큰의 subject(userPk)를 이용해 UserDetails를 조회
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        // Authentication 객체 생성해 반환
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 2. 토큰에서 회원 정보 추출(userPk)
    public String getUserPk(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 3. Request의 Header에서 token 값 가져오기
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 4. 토큰 유효성, 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
