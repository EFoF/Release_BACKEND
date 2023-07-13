package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.service.releasenote.domain.member.dto.MemberDTO.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    // @Valid는 SignUpRequest 에 걸려있는 유효성을 위배하는지 검사해줌.
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.signup(signUpRequest));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.signin(loginDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    // 회원 탈퇴
    @PostMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(HttpServletRequest request, @RequestBody WithDrawalDTO withDrawalDTO) {
        return ResponseEntity.ok(authService.withdrawal(request, withDrawalDTO.getInputPassword()));
    }

    @GetMapping("/getMemberId")
    public ResponseEntity<Long> getCurrentIdTest(){
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("memberId: {}", memberId);
        return ResponseEntity.ok(memberId);
    }
}
