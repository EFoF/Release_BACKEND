package com.service.releasenote.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.member.api.AuthController;
import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.application.EmailVerificationService;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.jwt.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.service.releasenote.domain.member.dto.MemberDTO.*;
import static com.service.releasenote.domain.member.dto.MailDTO.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class AuthControllerTest {
    @MockBean
    AuthService authService;

    @MockBean
    EmailVerificationService emailVerificationService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(sharedHttpSession())
                .build();
    }

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
                .password("test123!")
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();
    }

    public LoginDTO createLoginDTO(){
        return LoginDTO.builder()
                .email("test_email@test.com")
                .password("test123!")
                .build();
    }

    public WithDrawalDTO createWithDrawalDTO(){
        return WithDrawalDTO.builder()
                .inputPassword("test123!")
                .build();
    }

    public UpdatePasswordRequest createUpdatePasswordRequest(){
        return UpdatePasswordRequest.builder()
                .inputEmail("test_email@test.com")
                .inputOldPassword("test123!")
                .inputNewPassword("new-test123!")
                .build();
    }

    public EmailCodeRequestDTO createEmailCodeRequestDTO(){
        return EmailCodeRequestDTO.builder()
                .email("wlguswhd0809@naver.com")
                .build();
    }

    public EmailVerificationRequestDTO createEmailVerificationRequestDTO(){
        return EmailVerificationRequestDTO.builder()
                .email("wlguswhd0809@naver.com")
                .inputCode("abc123")
                .build();
    }

    // valid 실패한 경우 -> 컨트롤러 단에서 검증

    /**
     * 회원가입 , 로그인 테스트 (password 필드가 들어가는 test) 에서
     * 해당 DTO 에서 password 필드의 @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) 속성때문에
     * password 가 들어가지 않으므로, String 형을 만들어서 직접 Json 객체를 만든다.
     */
    @Test
    @DisplayName("성공 : 회원가입 테스트")
    public void signupForSuccess() throws Exception {
        //given
        SignUpRequest signUpRequest = createSignUpRequest();

        //when
        String s = objectMapper.writeValueAsString(signUpRequest);
        String password = signUpRequest.getPassword();
        String substring = s.substring(0, s.length() - 1);
        s = substring + ",\"password\":" + "\"" + password + "\"" + "}";

        //then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(s))
                .andExpect(content().string("회원 가입에 성공했습니다."))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("성공 : 로그인 테스트")
    public void signinForSuccess() throws Exception {
        //given
        LoginDTO loginDTO = createLoginDTO();

        String s = objectMapper.writeValueAsString(loginDTO);
        String password = loginDTO.getPassword();
        String substring = s.substring(0, s.length() - 1);
        s = substring + ",\"password\":" + "\"" + password + "\"" + "}";

        //when

        //then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(s))
                .andExpect(content().string("로그인 되었습니다.")) //httpHeaders 들어가야함
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공 : 로그아웃 테스트") // 토큰을 추가 안 했는데 어떻게 통과되지?
    public void logoutForSuccess() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(content().string("로그아웃 되었습니다."))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공 : reissue 테스트")
    public void reissueForSuccess() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(content().string("")) //tokenInfoDTO 들어가야함
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 : 회원 탈퇴 테스트")
    public void withdrawalForSuccess() throws Exception {
        //given
        WithDrawalDTO withDrawalDTO = createWithDrawalDTO();

        //when

        //then
        mockMvc.perform(patch("/auth/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withDrawalDTO))
                )
                .andExpect(content().string("회원 탈퇴 처리되었습니다."))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 : 비밀번호 변경 테스트 - 로그인 유저")
    public void updatePasswordByLoggedInUserForSuccess() throws Exception {
        //given
        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        //when

        //then
        mockMvc.perform(patch("/auth/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest))
                )
                .andExpect(content().string("비밀번호가 변경 되었습니다."))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공 : 비밀번호 변경 테스트 - 비로그인 유저")
    public void updatePasswordByAnonymousUserForSuccess() throws Exception {
        //given
        UpdatePasswordRequest updatePasswordRequest = createUpdatePasswordRequest();

        //when

        //then
        mockMvc.perform(patch("/auth/update/password/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest))
                )
                .andExpect(content().string("비밀번호가 변경 되었습니다."))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공 : 이메일 송신 테스트")
    public void sendEmailVerificationCodeForSuccess() throws Exception {
        //given
        EmailCodeRequestDTO emailCodeRequestDTO = createEmailCodeRequestDTO();

        //when

        //then
        mockMvc.perform(post("/auth/mail/sending")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailCodeRequestDTO))
                )
                .andExpect(content().string("인증 코드가 발송됐습니다."))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공 : 이메일 인증 코드 인증 테스트")
    public void verifyEmailVerificationCodeForSuccess() throws Exception {
        //given
        EmailVerificationRequestDTO emailVerificationRequestDTO = createEmailVerificationRequestDTO();

        //when
        when(emailVerificationService.verifyEmailVerificationCode(any())).thenReturn(true);

        //then
        mockMvc.perform(post("/auth/mail/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerificationRequestDTO))
                )
                .andExpect(content().string(String.valueOf(true)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 : 사용자 정보 불러오기 테스트")
    public void findMemberByMemberIdForSuccess() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(get("/auth/member/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(content().string(""))
                .andExpect(status().isOk());
    }
}
