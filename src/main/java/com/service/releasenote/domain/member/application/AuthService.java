package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.*;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.global.jwt.JwtFilter;
import com.service.releasenote.global.jwt.TokenProvider;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.service.releasenote.domain.member.dto.MemberDTO.*;
import static com.service.releasenote.domain.token.dto.TokenDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final JwtFilter jwtFilter;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    // 회원가입 로직
    public ResponseEntity<?> signup(SignUpRequest signUpRequest) {
        Optional<Member> member = memberRepository.findByEmail(signUpRequest.getEmail());

        if (member.isPresent()) { // 이미 가입한 유저인지 검증
            if (member.get().isDeleted()){ // 탈퇴한 유저인지 검증
                throw new DeletedMemberException();
            }
            throw new MemberAlreadyExistsException();
        }

        // 유저 생성
        Member memberBuilder = Member.builder()
                .userName(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();

        memberRepository.save(memberBuilder);

        log.info("회원 가입");

        return new ResponseEntity<>("회원 가입에 성공했습니다.", HttpStatus.CREATED);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> signin(LoginDTO loginDTO) {
        // 로그인 정보로 AuthenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        try {
            // 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            Member member = memberRepository.findById(Long.valueOf(authentication.getName())).orElseThrow(UserNotFoundException::new);

            if (member.isDeleted()){ // 탈퇴한 유저인지 검증
                throw new DeletedMemberException();
            }

            // 인증 정보를 기반으로 JWT 토큰 생성
            TokenInfoDTO tokenInfoDTO = tokenProvider.createToken(authentication);

            // Refresh Token 을 Redis 에 저장 (expirationTime 설정을 통해 자동 삭제 처리)
            stringRedisTemplate.opsForValue().set("RT:" + authentication.getName(), tokenInfoDTO.getRefreshToken(),
                    tokenInfoDTO.getRefreshTokenExpiresIn(), TimeUnit.MILLISECONDS);

            // Access Token 을 Header 에 추가
            String accessToken = tokenInfoDTO.getAccessToken();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.remove(JwtFilter.AUTHORIZATION_HEADER); // Access Token Flush
//            httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
            httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + accessToken);

            log.info("로그인");

            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Header 에서 Access Token 추출
        String accessToken = jwtFilter.resolveToken(request);

        // Access Token 검증
        if (!tokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException();
        }

        // Access Token 으로 Authentication 만듦
        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // Redis 에서 해당 pk 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (stringRedisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            stringRedisTemplate.delete("RT:" + authentication.getName());
        }

        // 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = tokenProvider.getExpiration(accessToken);
        stringRedisTemplate.opsForValue()
                .set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        log.info("로그아웃");

        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> reissue(HttpServletRequest request) {
        String accessToken = jwtFilter.resolveToken(request);

        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // Redis 에서 pk 를 기반으로 저장된 Refresh Token 값을 가져옴
        String refreshToken = stringRedisTemplate.opsForValue().get("RT:" + authentication.getName());

        // 로그아웃되어 Redis 에 Refresh Token 이 삭제되어 존재하지 않는 경우 처리
        if (ObjectUtils.isEmpty(refreshToken)) {
            log.info("Refresh Token 정보가 존재하지 않습니다.");
            throw new InvalidTokenException();
        }

        // Refresh Token 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            log.info("Refresh Token 정보가 유효하지 않습니다.");
            throw new InvalidTokenException();
        }

//        // Refresh Token 이 존재하지만 동일하지 않을 경우 처리
//        if(!refreshToken.equals(allTokenDTO.getRefreshToken())) {
//            return new ResponseEntity<>("Refresh Token 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
//        }

        // 새로운 토큰 생성
        TokenInfoDTO tokenInfoDTO = tokenProvider.createToken(authentication);

        // Refresh Token Redis 업데이트
        stringRedisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfoDTO.getRefreshToken(),
                        tokenInfoDTO.getRefreshTokenExpiresIn(), TimeUnit.MILLISECONDS);

        log.info("reissue!");
        return new ResponseEntity<>(tokenInfoDTO, HttpStatus.OK);
    }

    @Transactional
    // 회원 탈퇴
    public ResponseEntity<?> withdrawal(HttpServletRequest request, WithDrawalDTO withDrawalDTO) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        String inputPassword = withDrawalDTO.getInputPassword();

        boolean isPasswordMatch = passwordEncoder.matches(inputPassword, originPassword);

        if (!isPasswordMatch) { // 입력한 비밀번호가 틀렸을 경우
            throw new InvalidPasswordException();
        }

        logout(request); // 로그아웃 진행

        member.setDeleted(true); // isDeleted 필드 True 로 전환.

        log.info("회원 탈퇴");

        return new ResponseEntity<>("회원 탈퇴 처리되었습니다.", HttpStatus.OK);
    }

    @Transactional
    // 로그인 되어 있는 유저의 비밀번호 변경
    public ResponseEntity<?> updatePasswordByLoggedInUser(UpdatePasswordRequest updatePasswordRequest) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        String inputOldPassword = updatePasswordRequest.getInputOldPassword();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        Member member = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        boolean isPasswordMatch = passwordEncoder.matches(inputOldPassword, originPassword);
        boolean isDuplicatedPassword = passwordEncoder.matches(inputNewPassword, originPassword);

        if (!isPasswordMatch) { // 입력한 비밀번호가 틀렸을 경우
            throw new InvalidPasswordException();
        }

        if (isDuplicatedPassword) { // 변경할 비밀번호가 기존 비밀번호와 일치할 경우
            throw new DuplicatedPasswordException();
        }

        member.setPassword(passwordEncoder.encode(inputNewPassword));

        log.info("비밀번호 변경 - 로그인 유저");

        return new ResponseEntity<>("비밀번호가 변경 되었습니다.", HttpStatus.OK);
    }

    @Transactional
    // 로그인 되어 있는 않은 유저의 비밀번호 변경
    public ResponseEntity<?> updatePasswordByAnonymousUser(UpdatePasswordRequest updatePasswordRequest) {
        String inputEmail = updatePasswordRequest.getInputEmail();
        String inputNewPassword = updatePasswordRequest.getInputNewPassword();

        Member member = memberRepository.findByEmail(inputEmail).orElseThrow(UserNotFoundException::new);

        String originPassword = member.getPassword(); // DB에 저장되어 있는 기존 비밀번호

        boolean isDuplicatedPassword = passwordEncoder.matches(inputNewPassword, originPassword);

        if (isDuplicatedPassword) { // 변경할 비밀번호가 기존 비밀번호와 일치할 경우
            throw new DuplicatedPasswordException();
        }

        member.setPassword(passwordEncoder.encode(inputNewPassword));

        log.info("비밀번호 변경 - 비로그인 유저");

        return new ResponseEntity<>("비밀번호가 변경 되었습니다.", HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public MemberResponseDTO findMemberByMemberId(){
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);

        String userName = member.getUserName();
        String email = member.getEmail();

        MemberResponseDTO memberResponseDTO = MemberResponseDTO.builder()
                .username(userName)
                .email(email)
                .build();

        return memberResponseDTO;
    }
}