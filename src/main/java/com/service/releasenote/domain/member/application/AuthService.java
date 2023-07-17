package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
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
        // 이미 DB 안에 있는지 검사
        if (memberRepository.findByEmail(signUpRequest.getEmail()).orElse(null) != null) {
            return new ResponseEntity<>("이미 가입되어 있는 유저입니다.", HttpStatus.BAD_REQUEST);
//            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        // 유저 생성
        Member member = Member.builder()
                .userName(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();

        log.info("회원가입");

        Member save = memberRepository.save(member);

        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> signin(@Valid LoginDTO loginDTO){
        // 로그인 정보로 AuthenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        try{
            // 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

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

            return new ResponseEntity<>(tokenInfoDTO, httpHeaders, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request){
        // Header 에서 Access Token 추출
        String accessToken = jwtFilter.resolveToken(request);

        // Access Token 검증
        if (!tokenProvider.validateToken(accessToken)) {
            log.info("access token: {}",accessToken);
            return new ResponseEntity<>("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
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
        if(ObjectUtils.isEmpty(refreshToken)) {
            return new ResponseEntity<>("Refresh Token 정보가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // Refresh Token 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            return new ResponseEntity<>("Refresh Token 정보가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<?> withdrawal(HttpServletRequest request, String inputPassword){
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);

        String password = member.getPassword();

        boolean matches = passwordEncoder.matches(inputPassword, password);

        if (matches) { // 입력한 비밀번호가 맞았을 경우
            logout(request); // 로그아웃 진행
            member.setDeleted(true); // isDeleted 필드 True 로 전환.

            log.info("회원 탈퇴");

            return new ResponseEntity<>("회원 탈퇴 처리되었습니다.", HttpStatus.OK);
        }
        else { // 입력한 비밀번호가 틀렸을 경우
            return new ResponseEntity<>("비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}