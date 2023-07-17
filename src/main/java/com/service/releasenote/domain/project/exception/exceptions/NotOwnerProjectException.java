package com.service.releasenote.domain.project.exception.exceptions;

public class NotOwnerProjectException extends RuntimeException{
    public NotOwnerProjectException() { super("삭제할 권한이 없습니다."); }
}
