package com.service.releasenote.domain.project.exception.handler;

import com.service.releasenote.domain.project.exception.exceptions.DuplicatedProjectTitleException;
import com.service.releasenote.domain.project.exception.exceptions.NotOwnerProjectException;
import com.service.releasenote.global.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;


@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(DuplicatedProjectTitleException.class)
    public final ResponseEntity<String> handleDuplicatedProjectTitleException(DuplicatedProjectTitleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NotOwnerProjectException.class)
    public final ResponseEntity<String> handleNotOwnerProjectException(NotOwnerProjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
