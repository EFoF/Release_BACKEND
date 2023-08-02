//package com.service.releasenote.auth;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.service.releasenote.domain.member.api.AuthController;
//import com.service.releasenote.domain.member.application.AuthService;
//import com.service.releasenote.domain.member.dto.MemberDTO;
//import com.service.releasenote.domain.member.model.Authority;
//import com.service.releasenote.domain.member.model.Member;
//import com.service.releasenote.domain.member.model.MemberLoginType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import static com.service.releasenote.domain.member.dto.MemberDTO.*;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(AuthController.class)
//@MockBean(JpaMetamodelMappingContext.class)
//public class AuthControllerTest {
//    @MockBean
//    AuthService authService;
//
//    @MockBean
//    PasswordEncoder passwordEncoder;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    public Member buildMember(Long id) { // Test 용 멤버 생성
//        return Member.builder()
//                .id(id)
//                .userName("test_user_name")
//                .email("test_email@test.com")
//                .password(passwordEncoder.encode("test_password"))
//                .authority(Authority.ROLE_USER)
//                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
//                .isDeleted(false)
//                .build();
//    }
//
//    public MemberDTO.SignUpRequest createSignUpRequest(){
//        return MemberDTO.SignUpRequest.builder()
//                .username("test_user_name")
//                .email("test_email@test.com")
//                .password("test_password")
//                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
//                .build();
//    }
//
//    @Test
//    @DisplayName("성공 : 회원가입 테스트")
//    public void signupForSuccess() throws Exception {
//        //given
//        Member member = buildMember(1L);
//        SignUpRequest signUpRequest = createSignUpRequest();
////        ResponseEntity<String> mockResponse = new ResponseEntity<>("회원 가입에 성공했습니다.", HttpStatus.CREATED);
//        String mockResponse = "회원 가입에 성공했습니다.";
//
//        //when
////        when(authService.signup(any())).thenReturn(mockResponse);
////        when(authService.signup(any())).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.CREATED));
//
//        //then
//        mockMvc.perform(post("/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(signUpRequest)))
//                .andExpect(content().string(mockResponse))
//                .andExpect(status().isOk());
//    }
//}
