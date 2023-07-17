package com.service.releasenote.domain.project.exception.exceptions;

public class DuplicatedProjectTitleException extends RuntimeException{
    public DuplicatedProjectTitleException() { super("이미 존재하는 프로젝트 제목입니다."); }
}
