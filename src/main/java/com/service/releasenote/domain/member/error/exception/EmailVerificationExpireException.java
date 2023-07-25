package com.service.releasenote.domain.member.error.exception;

public class EmailVerificationExpireException extends IllegalArgumentException{
    public EmailVerificationExpireException() {
        super("이메일 인증 코드가 만료되었습니다.");
    }
}
