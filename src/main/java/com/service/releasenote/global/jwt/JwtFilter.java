package com.service.releasenote.global.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    // JWT 토큰의 인증 정보를 SecurityContext 에 저장
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String jwt = resolveToken(request);
        String requestURI = request.getRequestURI();

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // jwt 가 존재하고 유효한 토큰인지 검증

            String isLogout = stringRedisTemplate.opsForValue().get(jwt); // // Redis 에 해당 AccessToken Logout 여부 확인

            if (ObjectUtils.isEmpty(isLogout)){ // isLogout 이 null 이거나 비어있는 상태라면. 즉, black list 로 등록되지 않았다면.

                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
            }
        } else {
            log.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // Authorization 헤더 값을 가져옴.
        // Authorization 헤더 값 확인
//        log.info("bearerToken: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) { // bearerToken 값이 null 이 아니고, "Bearer "로 시작하는지 검사
            //Bearer 인증 스킴을 사용하고 있기 때문에, "Bearer " 이후부터 token 값이 시작되므로, bearerToken.substring(7)를 통해 "Bearer " 이후의 값만 가져옴
            return bearerToken.substring(7);
        }

        return null;
    }
}
