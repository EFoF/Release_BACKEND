package com.service.releasenote.global.error.exception;

import org.springframework.security.core.AuthenticationException;

public class UnAuthorizedException extends AuthenticationException {
    public UnAuthorizedException() {
        super("Security Context에 인증 정보가 없습니다.");
    }
}
