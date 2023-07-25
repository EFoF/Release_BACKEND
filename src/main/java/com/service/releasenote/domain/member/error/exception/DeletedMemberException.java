package com.service.releasenote.domain.member.error.exception;

public class DeletedMemberException extends IllegalArgumentException{
    public DeletedMemberException() {
        super("회원 탈퇴한 계정입니다.");
    }
}
