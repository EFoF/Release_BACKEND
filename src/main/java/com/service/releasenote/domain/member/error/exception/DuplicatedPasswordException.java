package com.service.releasenote.domain.member.error.exception;

public class DuplicatedPasswordException extends IllegalArgumentException{
    public DuplicatedPasswordException() {
        super("기존과 동일한 비밀번호로 변경할 수 없습니다.");
    }
}
