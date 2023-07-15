package com.service.releasenote.domain.project.exception.handler;

import com.service.releasenote.domain.project.exception.exceptions.DuplicatedProjectTitleException;
import com.service.releasenote.global.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static com.service.releasenote.domain.project.exception.response.ProjectErrorResponse.DUPLICATED_PROJECT_TITLE;

@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(DuplicatedProjectTitleException.class)
    public final ResponseEntity<ErrorResponse> handleDuplicatedParticipateException(DuplicatedProjectTitleException ex, WebRequest request) {
//        log.info(request.getDescription(false));
        return DUPLICATED_PROJECT_TITLE;
    }
}
