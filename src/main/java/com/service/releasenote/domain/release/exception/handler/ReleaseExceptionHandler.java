package com.service.releasenote.domain.release.exception.handler;

import com.service.releasenote.domain.release.exception.ReleasesNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ReleaseExceptionHandler {
    @ExceptionHandler(ReleasesNotFoundException.class)
    protected final ResponseEntity<String> notFoundRelease(ReleasesNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
