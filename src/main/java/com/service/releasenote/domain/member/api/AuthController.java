package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.application.EmailVerificationService;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.service.releasenote.domain.member.dto.MemberDTO.*;
import static com.service.releasenote.domain.token.dto.TokenDTO.*;
import static com.service.releasenote.domain.member.dto.MailDTO.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입에 성공했습니다.");
    }
    @PostMapping("/signin")
    public ResponseEntity<HttpHeaders> signin(@Valid @RequestBody LoginDTO loginDTO) {
        HttpHeaders httpHeaders = authService.signin(loginDTO);
        return ResponseEntity.ok(httpHeaders);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenInfoDTO> reissue(HttpServletRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    // 회원 탈퇴
    @PatchMapping("/withdrawal")
    public ResponseEntity<String> withdrawal(HttpServletRequest request, @RequestBody WithDrawalDTO withDrawalDTO) {
        authService.withdrawal(request, withDrawalDTO);
        return ResponseEntity.ok("회원 탈퇴 처리되었습니다.");
    }

    // 로그인 되어 있는 유저의 비밀번호 변경
    @PatchMapping("/update/password")
    public ResponseEntity<String> updatePasswordByLoggedInUser(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        authService.updatePasswordByLoggedInUser(updatePasswordRequest);
        return ResponseEntity.ok("비밀번호가 변경 되었습니다.");
    }

    // 로그인 되어 있는 않은 유저의 비밀번호 변경
    @PatchMapping("/update/password/anonymous")
    public ResponseEntity<String> updatePasswordByAnonymousUser(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        authService.updatePasswordByAnonymousUser(updatePasswordRequest);
        return ResponseEntity.ok("비밀번호가 변경 되었습니다.");
    }

    @PostMapping("/mail/sending")
    public ResponseEntity<String> sendEmailVerificationCode(
            @RequestBody @Valid EmailCodeRequestDTO emailCodeRequestDTO) throws Exception {
        emailVerificationService.sendSimpleMessage(emailCodeRequestDTO);
        return ResponseEntity.ok("인증 코드가 발송됐습니다.");
    }

    @PostMapping("/mail/verification")
    public ResponseEntity<Boolean> verifyEmailVerificationCode(
            @RequestBody @Valid EmailVerificationRequestDTO emailVerificationRequestDTO) {
        return ResponseEntity.ok(emailVerificationService.verifyEmailVerificationCode(emailVerificationRequestDTO));
    }

    @GetMapping("/member/info")
    public ResponseEntity<MemberResponseDTO> findMemberByMemberId(){
        return ResponseEntity.ok(authService.findMemberByMemberId());
    }

    // 현재 로그인 된 멤버의 pk값 - test 용
    @GetMapping("/getMemberId")
    public ResponseEntity<Long> getCurrentIdTest(){
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("memberId: {}", memberId);
        return ResponseEntity.ok(memberId);
    }
}
