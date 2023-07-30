package com.service.releasenote.domain.project.exception.exceptions;

public class MemberProjectNotFoundException extends RuntimeException{

    public MemberProjectNotFoundException() {
        super("해당 프로젝트와 멤버로 존재하는 데이터가 없습니다.");
    }

    public MemberProjectNotFoundException(String message) {
        super(message);
    }
}
