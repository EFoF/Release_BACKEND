package com.service.releasenote.domain.member.error.exception;

public class UserPermissionDeniedException extends RuntimeException{
    public UserPermissionDeniedException() { super("회사에 속한 사용자가 아닙니다.");}
}
