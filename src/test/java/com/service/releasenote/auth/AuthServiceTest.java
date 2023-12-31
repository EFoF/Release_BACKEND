package com.service.releasenote.auth;

import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.*;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import com.service.releasenote.global.jwt.JwtFilter;
import com.service.releasenote.global.jwt.TokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

import static com.service.releasenote.domain.member.dto.MemberDTO.*;

@SpringBootTest // Spring Security 때문에 넣어야함, 전체 컨텍스트를 로드함.
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class) // JUnit 5에서 사용되는 어노테이션, 테스트 확장을 지원, Mockito 를 사용하겠다는 뜻.
public class AuthServiceTest {
    @MockBean
    MemberRepository memberRepository;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    PasswordEncoder passwordEncoder;

//    @MockBean
//    @Autowired
    @Autowired
    AuthenticationManagerBuilder authenticationManagerBuilder;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AuthService authService;

    private static MockHttpServletRequest request;

    public Member buildMember(Long id) { // Test 용 멤버 생성
        return Member.builder()
                .id(id)
                .userName("test_user_name")
                .email("test_email@test.com")
                .password(passwordEncoder.encode("test_password"))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .isDeleted(false)
                .build();
    }

    public SignUpRequest createSignUpRequest(){
        return SignUpRequest.builder()
                .username("test_user_name")
                .email("test_email@test.com")
                .password("test_password")
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();
    }

    public LoginDTO createLoginDTO(){
        return LoginDTO.builder()
                .email("test_email@test.com")
                .password("test_password")
                .build();
    }

    public WithDrawalDTO createWithDrawalDTO(){
        return WithDrawalDTO.builder()
                .inputPassword("test_password")
                .build();
    }

    public UpdatePasswordRequest createUpdatePasswordRequest(){
        return UpdatePasswordRequest.builder()
                .inputEmail("test_email@test.com")
                .inputOldPassword("test_password")
                .inputNewPassword("new_test_password")
                .build();
    }

    @BeforeAll
    public static void setup() throws IOException {
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
    }

    // valid 실패한 경우 -> 컨트롤러 단에서 검증

//    @WithAnonymousUser
//    @WithMockUser
//    @WithMockCustomUser

    @Test
    @DisplayName("성공 : 회원가입 테스트")
    public void signupForSuccess() throws Exception {
        //given -> 초기 상태를 설정
        SignUpRequest signUpRequest = createSignUpRequest();
        Member member = buildMember(1L);

        //when -> 특정 조건 또는 동작을 시뮬레이션하는 단계
        when(memberRepository.findByEmail(signUpRequest.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.save(member)).thenReturn(member);

        //then -> 특정 결과를 검증하는 단계
    }

    @Test
    @DisplayName("실패 : 회원가입 테스트 - 이미 가입되어 있는 경우")
    public void signupForFailureByAlreadyExistsMember() throws Exception {
        //given -> 초기 상태를 설정
        SignUpRequest signUpRequest = createSignUpRequest();
        Member member = buildMember(1L);

        //when -> 특정 조건 또는 동작을 시뮬레이션하는 단계
        when(memberRepository.findByEmail(signUpRequest.getEmail())).thenReturn(Optional.ofNullable(member));
        when(memberRepository.save(member)).thenReturn(member);

        //then -> 특정 결과를 검증하는 단계
        Assertions.assertThrows(MemberAlreadyExistsException.class, () -> authService.signup(signUpRequest));
    }

    @Test
    @DisplayName("실패 : 회원가입 테스트 - 이미 탈퇴한 경우")
    public void signupForFailureByDeletedMember() throws Exception {
        //given -> 초기 상태를 설정
        SignUpRequest signUpRequest = createSignUpRequest();
        Member member = buildMember(1L);
        member.setDeleted(true);

        //when -> 특정 조건 또는 동작을 시뮬레이션하는 단계
        when(memberRepository.findByEmail(signUpRequest.getEmail())).thenReturn(Optional.ofNullable(member));
        when(memberRepository.save(member)).thenReturn(member);

        //then -> 특정 결과를 검증하는 단계
        Assertions.assertThrows(DeletedMemberException.class, () -> authService.signup(signUpRequest));
    }

//    @Test
//    @DisplayName("성공 : 로그인 테스트")
//    public void signinForSuccess() throws Exception {
//        //given
//        Member member = buildMember(1L);
//        LoginDTO loginDTO = createLoginDTO();
//
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
//
//        // Mock AuthenticationManager
//        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
//        when(authenticationManager.authenticate(authenticationToken)).thenReturn(
//                new UsernamePasswordAuthenticationToken(member, null, null)
//        );
//
//        // Mock SecurityContextHolder
//        SecurityContextHolder.setContext(new SecurityContextImpl());
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//
//        System.out.println("authenticationToken = " + authenticationToken);
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        //when
//        when(memberRepository.findById(Long.valueOf(authentication.getName()))).thenReturn(Optional.ofNullable(member));
//
//        //then
////        HttpHeaders headers = authService.signin(loginDTO);
////        assertThat(headers.equals());
//    }

//    @Test
//    @DisplayName("실패 : 로그인 테스트 - 아이디 / 비밀번호 틀렸을 시")
//    public void signinForFailureByBadCredentials() throws Exception {
//        //given
//        Member member = buildMember(1L);
//        LoginDTO loginDTO = createLoginDTO();
//
////        UsernamePasswordAuthenticationToken authenticationToken =
////                new UsernamePasswordAuthenticationToken(loginDTO.getEmail()+"wrong", loginDTO.getPassword()+"wrong");
////
////        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        //when
////        when(memberRepository.findById(Long.valueOf(authentication.getName()))).thenReturn(Optional.ofNullable(member));
//        when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(member));
////        when(memberRepository.findById(Long.valueOf(authentication.getName())).get().isDeleted()).thenReturn(false);
//
//        //then
////        Assertions.assertThrows(BadCredentialsException.class, () -> authService.signin(loginDTO));
//        Assertions.assertThrows(InvalidCredentialsException.class, () -> authService.signin(loginDTO));
//    }
//
//    @Test
//    @DisplayName("실패 : 로그인 테스트 - 이미 탈퇴한 경우")
//    public void signinForFailureByDeletedMember() throws Exception {
//        //given
//        Member member = buildMember(1L);
//        LoginDTO loginDTO = createLoginDTO();
//
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
//
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        //when
//        when(memberRepository.findById(Long.valueOf(authentication.getName()))).thenReturn(Optional.ofNullable(member));
//        when(memberRepository.findById(Long.valueOf(authentication.getName())).get().isDeleted()).thenReturn(true);
//
//        //then
//        Assertions.assertThrows(DeletedMemberException.class, () -> authService.signin(loginDTO));
//    }
//
    @Test
    @DisplayName("성공 : 로그아웃 테스트")
    public void logoutForSuccess() throws Exception {
        //given
        Member member = buildMember(1L);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String accessToken = jwtFilter.resolveToken(request);

        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        //when
        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
    }

//    @Test
//    @DisplayName("실패 : 로그아웃 테스트 - 인증되지 않은 사용자")
//    public void logoutForFailureByUnAuthorizedUser() throws Exception {
//        //given
//        Member member = buildMember(1L);
//        String accessToken = jwtFilter.resolveToken(request);
//
//        String username = member.getUserName();
//        String refreshTokenKey = "RT:" + username;
//
//
//        //when
//        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
//        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
//        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(valueOperations.get(refreshTokenKey)).thenReturn("some_refresh_token");
////        when(stringRedisTemplate.opsForValue().get(any())).thenReturn(null);
////        when(mockValueOps.get(any())).thenReturn(null);
//
//        //then
//        Assertions.assertThrows(AuthenticationException.class, () -> authService.logout(request));
//    }

    @Test
    @DisplayName("실패 : 로그아웃 테스트 - Access Token 유효하지 않을 경우")
    public void logoutForFailureByInvalidToken() throws Exception {
        //given
        Member member = buildMember(1L);

        String accessToken = jwtFilter.resolveToken(request);

        //when
        when(tokenProvider.validateToken(accessToken)).thenReturn(false);

        //then
        Assertions.assertThrows(InvalidTokenException.class, () -> authService.logout(any()));
    }

//    @Test
//    @WithMockCustomUser
//    @DisplayName("성공 : reissue 테스트")
//    public void reissueForSuccess() throws Exception {
//        //given
//        Member member = buildMember(1L);
//
//        String accessToken = jwtFilter.resolveToken(request);
//
//        Authentication authentication = tokenProvider.getAuthentication(accessToken);
//
//        String refreshToken = stringRedisTemplate.opsForValue().get("RT:" + authentication.getName());
//
//        //when
//        when(ObjectUtils.isEmpty(refreshToken)).thenReturn(false);
//        when(!tokenProvider.validateToken(refreshToken)).thenReturn(false);
//
//        //then
//        ResponseEntity<?> reissue = authService.reissue(any());
//        assertThat(reissue.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }

//    @Test
//    @DisplayName("실패 : reissue 테스트 - 인증되지 않은 사용자")
//    public void reissueForFailureByUnAuthorizedUser() throws Exception {
//        //given
//        Member member = buildMember(1L);
//
//        String accessToken = jwtFilter.resolveToken(any());
//
//        Authentication authentication = tokenProvider.getAuthentication(accessToken);
//
//        String refreshToken = stringRedisTemplate.opsForValue().get("RT:" + authentication.getName());
//
//        //when
//        when(ObjectUtils.isEmpty(refreshToken)).thenReturn(false);
//        when(!tokenProvider.validateToken(refreshToken)).thenReturn(false);
//
//        //then
//        Assertions.assertThrows(AuthenticationException.class, () -> authService.reissue(any()));
//    }
//
//    @Test
//    @WithMockCustomUser
//    @DisplayName("실패 : reissue 테스트 - Refresh Token 없을 경우")
//    public void reissueForFailureByRefreshTokenNotFound() throws Exception {
//        //given
//        Member member = buildMember(1L);
//
//        String accessToken = jwtFilter.resolveToken(any());
//
//        Authentication authentication = tokenProvider.getAuthentication(accessToken);
//
//        String refreshToken = stringRedisTemplate.opsForValue().get("RT:" + authentication.getName());
//
//        //when
//        when(ObjectUtils.isEmpty(refreshToken)).thenReturn(true);
//        when(!tokenProvider.validateToken(refreshToken)).thenReturn(false);
//
//        //then
//        Assertions.assertThrows(InvalidTokenException.class, () -> authService.reissue(any()));
//    }
//
//    @Test
//    @WithMockCustomUser
//    @DisplayName("실패 : reissue 테스트 - Refresh Token 유효하지 않을 경우")
//    public void reissueForFailureByInvalidRefreshToken() throws Exception {
//        //given
//        Member member = buildMember(1L);
//
//        String accessToken = jwtFilter.resolveToken(any());
//
//        Authentication authentication = tokenProvider.getAuthentication(accessToken);
//
//        String refreshToken = stringRedisTemplate.opsForValue().get("RT:" + authentication.getName());
//
//        //when
//        when(tokenProvider.validateToken(accessToken)).thenReturn(false);
//        when(!tokenProvider.validateToken(refreshToken)).thenReturn(true);
//
//        //then
//        Assertions.assertThrows(InvalidTokenException.class, () -> authService.reissue(any()));
//    }
//
    @Test
    @DisplayName("성공 : 회원탈퇴 테스트")
    public void withdrawalForSuccess() throws Exception {
        //given
        Member member = buildMember(1L);

        WithDrawalDTO withDrawalDTO = createWithDrawalDTO();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        String inputPassword = withDrawalDTO.getInputPassword();

        boolean isPasswordMatch = passwordEncoder.matches(inputPassword, originPassword);

        given(isPasswordMatch).willReturn(true);

        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));

        //then
    }

//    @Test
//    @DisplayName("실패 : 회원탈퇴 테스트 - 인증되지 않은 사용자")
//    public void withdrawalForFailureByUnAuthorizedUser() throws Exception {
//        //given
//        Member member = buildMember(1L);
//
//        WithDrawalDTO withDrawalDTO = createWithDrawalDTO();
//
//        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호
//
//        String inputPassword = withDrawalDTO.getInputPassword();
//
//        boolean isPasswordMatch = passwordEncoder.matches(inputPassword, originPassword);
//
//        given(isPasswordMatch).willReturn(true);
//
//        //when
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));
//
//        //then
//        Assertions.assertThrows(UnAuthorizedException.class, () -> authService.withdrawal(request, withDrawalDTO, 1L));
//    }

    @Test
    @DisplayName("실패 : 회원탈퇴 테스트 - 비밀번호 틀렸을 경우")
    public void withdrawalForFailureInvalidPassword() throws Exception {
        //given
        Member member = buildMember(1L);

        WithDrawalDTO withDrawalDTO = createWithDrawalDTO();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        String inputPassword = withDrawalDTO.getInputPassword();

        boolean isPasswordMatch = passwordEncoder.matches(inputPassword, originPassword);

        given(isPasswordMatch).willReturn(false);

        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));

        //then
        Assertions.assertThrows(InvalidPasswordException.class, () -> authService.withdrawal(request, withDrawalDTO, 1L));
    }

    @Test
    @DisplayName("성공 : 비밀번호 변경(로그인) 테스트")
    public void updatePasswordByLoggedInUserForSuccess() throws Exception {
        //given
        Member member = buildMember(1L);

        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        String inputOldPassword = updatePasswordRequest.getInputOldPassword();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));
        when(passwordEncoder.matches(inputOldPassword, originPassword)).thenReturn(true);
        when(passwordEncoder.matches(inputNewPassword, originPassword)).thenReturn(false);

        //then
    }



    @Test
    @DisplayName("실패 : 비밀번호 변경(로그인) 테스트 - 입력한 비밀번호가 틀렸을 경우")
    public void updatePasswordByLoggedInUserForFailureByInvalidPassword() throws Exception {
        //given
        Member member = buildMember(1L);

        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        String inputOldPassword = updatePasswordRequest.getInputOldPassword();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));
        when(passwordEncoder.matches(inputOldPassword, originPassword)).thenReturn(false);
        when(passwordEncoder.matches(inputNewPassword, originPassword)).thenReturn(false);

        //then
        Assertions.assertThrows(InvalidPasswordException.class, () -> authService.updatePasswordByLoggedInUser(updatePasswordRequest, 1L));
    }

    @Test
    @DisplayName("실패 : 비밀번호 변경(로그인) 테스트 - 변경할 비밀번호가 기존 비밀번호와 일치할 경우")
    public void updatePasswordByLoggedInUserForFailureByDuplicatedPassword() throws Exception {
        //given
        Member member = buildMember(1L);

        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        String inputOldPassword = updatePasswordRequest.getInputOldPassword();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.ofNullable(member));
        when(passwordEncoder.matches(inputOldPassword, originPassword)).thenReturn(true);
        when(passwordEncoder.matches(inputNewPassword, originPassword)).thenReturn(true);

        //then
        Assertions.assertThrows(DuplicatedPasswordException.class, () -> authService.updatePasswordByLoggedInUser(updatePasswordRequest, 1L));
    }

    @Test
    @DisplayName("성공 : 비밀번호 변경(비 로그인) 테스트")
    public void updatePasswordByAnonymousUserForSuccess() throws Exception {
        //given
        Member member = buildMember(1L);

        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        String inputEmail = updatePasswordRequest.getInputEmail();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        //when
        when(memberRepository.findByEmail(inputEmail)).thenReturn(Optional.ofNullable(member));
        when(passwordEncoder.matches(inputNewPassword, originPassword)).thenReturn(false);

        //then
    }

    @Test
    @DisplayName("실패 : 비밀번호 변경(비 로그인) 테스트 - 변경할 비밀번호가 기존 비밀번호와 일치할 경우")
    public void updatePasswordByAnonymousUserForFailureByDuplicatedPassword() throws Exception {
        //given
        Member member = buildMember(1L);

        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        String inputEmail = updatePasswordRequest.getInputEmail();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        //when
        when(memberRepository.findByEmail(inputEmail)).thenReturn(Optional.ofNullable(member));
        when(passwordEncoder.matches(inputNewPassword, originPassword)).thenReturn(true);

        //then
        Assertions.assertThrows(DuplicatedPasswordException.class, () -> authService.updatePasswordByAnonymousUser(updatePasswordRequest));
    }
}
