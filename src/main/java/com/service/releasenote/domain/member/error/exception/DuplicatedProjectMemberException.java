package com.service.releasenote.domain.member.error.exception;

public class DuplicatedProjectMemberException extends RuntimeException{
    public DuplicatedProjectMemberException() {
        super("이미 프로젝트에 속한 사용자입니다.");
    }
}
