package com.service.releasenote.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.token.dto.TokenDTO.*;

@Slf4j
@Component
// JWT 토큰의 생성, 검증 및 파싱을 담당하는 인터페이스
public class TokenProvider implements InitializingBean {
    private static final String AUTHORITIES_KEY = "auth";
    private final String secret;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private Key key;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity) {
        this.secret = secret;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    @Override
    // 의존 관계가 끝나면 호출함.
    public void afterPropertiesSet(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenInfoDTO createToken(Authentication authentication){ // Authentication : 인증 정보 저장 객체
        String authorities = authentication.getAuthorities().stream() // stream : 컬렉션에 저장된 요소들을 하나씩 순회하면서 처리함
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // , 로 구분

        long now = new Date().getTime();
        Date accessValidity = new Date(now + this.accessTokenValidity); // access token 의 expire time 설정
        Date refreshValidity = new Date(now + this.refreshTokenValidity); // refresh token 의 expire time 설정

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(accessValidity)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(refreshValidity)
                .compact();

        return TokenInfoDTO.builder()
                .grantType("Bearer") // OAuth2 프로토콜에서 사용되는 필드
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessValidity.getTime())
                .refreshTokenExpiresIn(refreshValidity.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 코드
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 클레임 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetail 객체를 만듦.
        User principal = new User(claims.getSubject(), "", authorities);

        // 인증된 생성자인 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰의 유효성 검증을 수행하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();

        return (expiration.getTime() - now);
    }

}
