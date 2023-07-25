package com.service.releasenote.domain.member.error.exception;

public class InvalidCredentialsException extends IllegalArgumentException{
    public InvalidCredentialsException() {
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
