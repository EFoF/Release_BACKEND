package com.service.releasenote.domain.release.exception;

public class ReleasesNotFoundException extends RuntimeException {
    public ReleasesNotFoundException() {
        super("해당 릴리즈를 찾을 수 없습니다.");
    }

    public ReleasesNotFoundException(String s) {
        super(s);
    }
}
