package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.application.EmailVerificationService;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"auth"})
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @ApiOperation("API for sign up")
    @ApiResponses({
            @ApiResponse(code=201, message = "회원 가입 성공"),
            @ApiResponse(code=409, message = "이미 가입한 사용자"),
            @ApiResponse(code=409, message = "이미 탈퇴한 사용자")
    })
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입에 성공했습니다.");
    }

    @ApiOperation("API for sign in")
    @ApiResponses({
            @ApiResponse(code=200, message = "로그인 성공"),
            @ApiResponse(code=404, message = "DB에 존재하지 않는 사용자"),
            @ApiResponse(code=409, message = "이미 탈퇴한 사용자"),
            @ApiResponse(code=409, message = "아이디/비밀번호 불일치")
    })
    @PostMapping("/signin")
    public ResponseEntity<Void> signin(@Valid @RequestBody LoginDTO loginDTO) {
        HttpHeaders httpHeaders = authService.signin(loginDTO);
        return ResponseEntity.ok().headers(httpHeaders).build();
    }

    @ApiOperation("API for logout")
    @ApiResponses({
            @ApiResponse(code=200, message = "로그아웃 성공"),
            @ApiResponse(code=400, message = "유효하지 않은 토큰"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @ApiOperation("API for reissue")
    @ApiResponses({
            @ApiResponse(code=200, message = "reissue 성공"),
            @ApiResponse(code=400, message = "유효하지 않은 토큰"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자")
    })
    @PostMapping("/reissue")
    public ResponseEntity<TokenInfoDTO> reissue(HttpServletRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    // 회원 탈퇴
    @ApiOperation("API for withdrawal")
    @ApiResponses({
            @ApiResponse(code=200, message = "회원 탈퇴 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "DB에 존재하지 않는 사용자"),
            @ApiResponse(code=409, message = "비밀번호 불일치")
    })
    @PatchMapping("/withdrawal")
    public ResponseEntity<String> withdrawal(HttpServletRequest request, @RequestBody WithDrawalDTO withDrawalDTO) {
        authService.withdrawal(request, withDrawalDTO);
        return ResponseEntity.ok("회원 탈퇴 처리되었습니다.");
    }

    // 로그인 되어 있는 유저의 비밀번호 변경
    @ApiOperation("API for update password by logged in user")
    @ApiResponses({
            @ApiResponse(code=200, message = "비밀번호 변경 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "DB에 존재하지 않는 사용자"),
            @ApiResponse(code=409, message = "비밀번호 불일치"),
            @ApiResponse(code=409, message = "기존 비밀번호와 일치")
    })
    @PatchMapping("/update/password")
    public ResponseEntity<String> updatePasswordByLoggedInUser(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        authService.updatePasswordByLoggedInUser(updatePasswordRequest);
        return ResponseEntity.ok("비밀번호가 변경 되었습니다.");
    }

    // 로그인 되어 있는 않은 유저의 비밀번호 변경
    @ApiOperation("API for update password by anonymous user")
    @ApiResponses({
            @ApiResponse(code=200, message = "비밀번호 변경 성공"),
            @ApiResponse(code=404, message = "DB에 존재하지 않는 사용자"),
            @ApiResponse(code=409, message = "기존 비밀번호와 일치")
    })
    @PatchMapping("/update/password/anonymous")
    public ResponseEntity<String> updatePasswordByAnonymousUser(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        authService.updatePasswordByAnonymousUser(updatePasswordRequest);
        return ResponseEntity.ok("비밀번호가 변경 되었습니다.");
    }

    @ApiOperation("API for sending mail for sign up")
    @ApiResponses({
            @ApiResponse(code=200, message = "메일 송신 성공"),
            @ApiResponse(code=400, message = "메일 송신 실패")
    })
    @PostMapping("/mail/sending")
    public ResponseEntity<String> sendEmailVerificationCode(
            @RequestBody @Valid EmailCodeRequestDTO emailCodeRequestDTO) throws Exception {
        emailVerificationService.sendSimpleMessage(emailCodeRequestDTO);
        return ResponseEntity.ok("인증 코드가 발송됐습니다.");
    }

    @ApiOperation("API for verification mail code for sign up")
    @ApiResponses({
            @ApiResponse(code=200, message = "코드 인증 성공"),
            @ApiResponse(code=400, message = "코드 인증 실패")
    })
    @PostMapping("/mail/verification")
    public ResponseEntity<Boolean> verifyEmailVerificationCode(
            @RequestBody @Valid EmailVerificationRequestDTO emailVerificationRequestDTO) {
        return ResponseEntity.ok(emailVerificationService.verifyEmailVerificationCode(emailVerificationRequestDTO));
    }

    @ApiOperation("API for getting some member's information by using member's pk")
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자")
    })
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
