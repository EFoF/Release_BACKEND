package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.member.error.exception.EmailVerificationExpireException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.service.releasenote.domain.member.dto.MailDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    @Value("${spring.mail.username}")
    private String username;
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate stringRedisTemplate;
    private String verificationCode; // 인증 코드
    private long VERIFICATION_CODE_EXPIRE_TIME = 1000 * 60 * 3;  // 3분

    public MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : {}", to);
        log.info("인증 코드 : {}", verificationCode);

        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, to);// 메일을 보낼 대상
        message.setSubject("DOKLIB 회원가입 이메일 인증");// 제목

        String msg = "";
        msg += "<div style='margin:100px;'>";
        msg += "<h1> 안녕하세요!</h1>";
        msg += "<h1> 릴리즈 노트 공유시스템 플랫폼 DOKLIB 입니다.</h1>";
        msg += "<br>";
        msg += "<p>회원가입 창으로 돌아가 아래 코드를 입력해주세요.<p>";
        msg += "<br>";
        msg += "<p>감사합니다.<p>";
        msg += "<br>";
        msg += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msg += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msg += "<div style='font-size:130%'>";
        msg += "CODE : <strong>";
        msg += verificationCode + "</strong><div><br/> "; // 메일에 인증 코드 넣기
        msg += "</div>";

        message.setText(msg, "utf-8", "html"); // 내용, charset 타입, subtype
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(new InternetAddress(username+"@naver.com", "DOKLIB_ADMIN")); // 보내는 사람

        return message;
    }

    // 랜덤 인증 코드 생성
    public String createKey() {
        StringBuilder key = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) { // 인증 코드 6자리
            int index = random.nextInt(3);

            switch (index) {
                case 0:
                    key.append((char) (random.nextInt(26) + 97));
                    // a~z
                    break;
                case 1:
                    key.append((char) (random.nextInt(26) + 65));
                    // A~Z
                    break;
                case 2:
                    key.append((random.nextInt(10)));
                    // 0~9
                    break;
            }
        }

        return key.toString();
    }

    // 메일 발송
    public String sendSimpleMessage(EmailCodeRequestDTO emailCodeRequestDTO) throws Exception {
        String to = emailCodeRequestDTO.getEmail();

        verificationCode = createKey(); // 랜덤 인증 코드 생성

        MimeMessage message = createMessage(to); // 내가 전송할 메일의 내용을 담음

        try {
            javaMailSender.send(message); // 이메일 전송
            // Redis 에 사용자 이메일을 key, 인증 코드를 value 로 3분 동안 저장 시킴
            stringRedisTemplate.opsForValue().set(to, verificationCode, VERIFICATION_CODE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }

        return verificationCode;
    }

    // 인증 코드 검증
    public boolean verifyEmailVerificationCode(EmailVerificationRequestDTO emailVerificationRequestDTO){
        String email = emailVerificationRequestDTO.getEmail();
        String inputCode = emailVerificationRequestDTO.getInputCode();

        String validity = stringRedisTemplate.opsForValue().get(email); // Redis 에 저장돼있는 인증 코드

        if (validity == null){ // todo Empty 로 할지 null 로 할지 고민해야함
            throw new EmailVerificationExpireException();
        }
        if (!validity.equals(inputCode)){
            return false;
        }

        return true;
    }
}
