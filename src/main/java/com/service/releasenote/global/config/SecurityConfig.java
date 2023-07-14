package com.service.releasenote.global.config;

import com.service.releasenote.global.jwt.JwtAccessDeniedHandler;
import com.service.releasenote.global.jwt.JwtAuthenticationEntryPoint;
import com.service.releasenote.global.jwt.JwtSecurityConfig;
import com.service.releasenote.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
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
                .antMatchers("/error").permitAll() // 에러 코드 확인용
                // 로그인, 회원 가입, reissue 는 토큰이 없는 상태로 요청이 들어오므로 permitAll
                .antMatchers("/auth/signup").permitAll() // 회원 가입을 위한 api
                .antMatchers("/auth/signin").permitAll() // 로그인을 위한 api
                .antMatchers("/auth/reissue").permitAll() // reissue 를 위한 api
                .antMatchers("/auth/getMemberId").permitAll() // getCurrentId 를 위한 api
                .antMatchers("/swagger-ui/index.html").permitAll()
//                .anyRequest().authenticated() // 나머지 요청들은 모두 인증을 받아야 함
                .anyRequest().permitAll() // 나머지 요청들은 모두 인증을 받아야 함

                /** JwtSecurityConfig 적용 */
                .and()
                .apply(new JwtSecurityConfig(tokenProvider, stringRedisTemplate));

        return http.build();
    }

}
