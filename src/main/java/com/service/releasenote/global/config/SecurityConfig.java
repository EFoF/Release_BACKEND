package com.service.releasenote.global.config;

import com.service.releasenote.global.jwt.JwtAccessDeniedHandler;
import com.service.releasenote.global.jwt.JwtAuthenticationEntryPoint;
import com.service.releasenote.global.jwt.JwtSecurityConfig;
import com.service.releasenote.global.jwt.TokenProvider;
import com.service.releasenote.global.oauth.CustomOAuth2UserService;
import com.service.releasenote.global.oauth.handler.OAuth2FailureHandler;
import com.service.releasenote.global.oauth.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final StringRedisTemplate stringRedisTemplate;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /** 토큰 방식을 사용하기 때문에 csrf disable */
                .csrf().disable()

                /** 401, 403 Exception 핸들링 */
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                /** clickjacking 공격을 방지하는 X-Frame-Options 헤더 설정 */
                .and() // 보안 구성 체이닝(연결)
                .headers()
                .frameOptions()
                .sameOrigin()

                /** 세션 사용하지 않음 */
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                /** http 요청 접근 제한 */
                .and()
                .authorizeHttpRequests() // http 요청 접근 제한
                .antMatchers("/", "/error").permitAll() // 에러 코드 확인용
                // 로그인, 회원 가입, reissue 는 토큰이 없는 상태로 요청이 들어오므로 permitAll
                .antMatchers("/auth/signup").permitAll() // 회원 가입을 위한 api
                .antMatchers("/auth/signin").permitAll() // 로그인을 위한 api
                .antMatchers("/auth/reissue").permitAll() // reissue 를 위한 api
                .antMatchers("/auth/getMemberId").permitAll() // getCurrentId 를 위한 api
                .antMatchers("/auth/update/password/anonymous").permitAll() // 비로그인 유저를 위한 api
                .antMatchers(HttpMethod.GET, "/company/**").permitAll() // company로 시작하는 GET 방식만 허용
                .antMatchers("/swagger-ui/index.html").permitAll() // swagger를 위한 주소
                .anyRequest().authenticated() // 나머지 요청들은 모두 인증을 받아야 함

                /** JwtSecurityConfig 적용 */
                .and()
                .apply(new JwtSecurityConfig(tokenProvider, stringRedisTemplate))

                /** OAuth2 설정 */
                .and()
                .oauth2Login()
                .successHandler(oAuth2SuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                .failureHandler(oAuth2FailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                .userInfoEndpoint().userService(customOAuth2UserService); // customUserService 설정


        return http.build();
    }

}
