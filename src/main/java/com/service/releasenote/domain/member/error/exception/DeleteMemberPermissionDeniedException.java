package com.service.releasenote.domain.member.error.exception;

public class DeleteMemberPermissionDeniedException extends RuntimeException{
    public DeleteMemberPermissionDeniedException() {
        super("멤버를 삭제할 권한이 없습니다.");
    }
}
