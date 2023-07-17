package com.service.releasenote.domain.project.exception.exceptions;

public class ProjectPermissionDeniedException extends RuntimeException{
    public ProjectPermissionDeniedException() { super("프로젝트 수정 권한이 없습니다."); }
}
