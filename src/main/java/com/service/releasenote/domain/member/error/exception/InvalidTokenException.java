package com.service.releasenote.domain.member.error.exception;

public class InvalidTokenException extends IllegalArgumentException{
    public InvalidTokenException() {
        super("잘못된 요청입니다.");
    }
}
