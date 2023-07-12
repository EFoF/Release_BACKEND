package com.service.releasenote.global.error.exception;

import org.springframework.security.core.AuthenticationException;

public class NotSignInException extends AuthenticationException {
    public NotSignInException() {
        super("로그인되지 않은 사용자입니다.");
    }
}
