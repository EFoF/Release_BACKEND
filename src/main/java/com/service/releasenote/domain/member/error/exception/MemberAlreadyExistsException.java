package com.service.releasenote.domain.member.error.exception;

public class MemberAlreadyExistsException extends RuntimeException{
    public MemberAlreadyExistsException() {
        super("이미 가입되어 있는 유저입니다.");
    }
}
