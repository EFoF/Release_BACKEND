package com.service.releasenote.global.oauth.handler;

import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.global.jwt.JwtFilter;
import com.service.releasenote.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.service.releasenote.domain.token.dto.TokenDTO.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        String authoritiesString = authorities.toString();
        // 문자열 양 옆 대괄호 삭제
        String authoritiesWithoutBrackets = authoritiesString.substring(1, authoritiesString.length() - 1);

//        Map<String, Object> attributes = user.getAttributes();
//        log.info("attributes: {}", attributes);

        Member member = (Member) user.getAttributes().get("member");
        String memberId = member.getId().toString();

        // UserDetails 구현체 생성
        UserDetails userDetails = new User(memberId, "", Collections.singleton(new SimpleGrantedAuthority(authoritiesWithoutBrackets)));

        // Authentication 객체 생성
        Authentication userAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        //JWT 토큰 생성
        TokenInfoDTO tokenInfoDTO = tokenProvider.createToken(userAuthentication);

        // Refresh Token 을 Redis 에 저장
        stringRedisTemplate.opsForValue().set("RT:" + Long.valueOf(memberId), tokenInfoDTO.getRefreshToken(),
                tokenInfoDTO.getRefreshTokenExpiresIn(), TimeUnit.MILLISECONDS);

        String accessToken = tokenInfoDTO.getAccessToken();
        response.setHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + accessToken);

        log.info("간편 로그인 성공, 리다이렉트 실행");
        response.sendRedirect("/"); // 나중에 개발자 페이지로 리다이렉트 시켜야함.
    }
}
