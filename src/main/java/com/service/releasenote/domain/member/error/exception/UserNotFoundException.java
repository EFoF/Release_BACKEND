package com.service.releasenote.domain.member.error.exception;

public class UserNotFoundException extends IllegalArgumentException{
    public UserNotFoundException() {
        super("해당 사용자를 찾을 수 없습니다.");
    }
}
