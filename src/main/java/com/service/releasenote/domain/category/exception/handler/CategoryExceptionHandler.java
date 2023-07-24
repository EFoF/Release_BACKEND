package com.service.releasenote.domain.category.exception.handler;

import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.global.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CategoryExceptionHandler {


    protected final ResponseEntity<String> notFoundCategory(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
