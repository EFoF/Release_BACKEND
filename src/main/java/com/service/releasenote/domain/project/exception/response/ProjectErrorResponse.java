package com.service.releasenote.domain.project.exception.response;


import com.service.releasenote.domain.project.exception.exceptions.DuplicatedProjectTitleException;
import com.service.releasenote.global.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ProjectErrorResponse {
    public static final ResponseEntity<ErrorResponse> DUPLICATED_PROJECT_TITLE =
            new ResponseEntity<>(ErrorResponse.builder()
            .exceptionName(DuplicatedProjectTitleException.class.getSimpleName())
            .message("이미 존재하는 프로젝트입니다.")    // 포스트맨에 반영되는 부분
            .build(), HttpStatus.CONFLICT);

}
