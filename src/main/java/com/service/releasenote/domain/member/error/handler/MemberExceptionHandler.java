package com.service.releasenote.domain.member.error.handler;

import com.service.releasenote.domain.member.error.exception.DuplicatedProjectMemberException;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.error.exception.UserPermissionDeniedException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    protected final ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserPermissionDeniedException.class)
    public final ResponseEntity<String> handleUserPermissionDeniedException(UserPermissionDeniedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicatedProjectMemberException.class)
    public final ResponseEntity<String> handleUserPermissionDeniedException(DuplicatedProjectMemberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
