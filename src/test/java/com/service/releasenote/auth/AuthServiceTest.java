package com.service.releasenote.auth;

import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.jwt.JwtFilter;
import com.service.releasenote.global.jwt.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest // Spring Security 때문에 넣어야함, 전체 컨텍스트를 로드함.
@ExtendWith(MockitoExtension.class) // JUnit 5에서 사용되는 어노테이션, 테스트 확장을 지원, Mockito 를 사용하겠다는 뜻.
public class AuthServiceTest {
    @MockBean
    MemberRepository memberRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AuthService authService;

    public Member buildMember(Long id) { // Test 용 멤버 생성
        return Member.builder()
                .id(id)
                .userName("test_user_name")
                .email("test_email@test.com")
                .password(passwordEncoder.encode("test_password"))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();
    }

//    @WithAnonymousUser
//    @WithMockUser
//    @WithMockCustomUser

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 회원가입 테스트")
    public void signupForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 로그인 테스트")
    public void signinForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 로그아웃 테스트")
    public void logoutForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - reissue 테스트")
    public void reissueForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 회원탈퇴 테스트")
    public void withdrawalForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 비밀번호 변경(로그인) 테스트")
    public void updatePasswordByLoggedInUserForSuccess() throws Exception {

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 비밀번호 변경(비 로그인) 테스트")
    public void updatePasswordByAnonymousUserForSuccess() throws Exception {

    }
}
