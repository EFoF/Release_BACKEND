package com.service.releasenote.domain.project.exception.exceptions;

public class CompanyNotFoundException extends RuntimeException{
    public CompanyNotFoundException() {
        super("회사를 찾을 수 없습니다.");
    }
}
