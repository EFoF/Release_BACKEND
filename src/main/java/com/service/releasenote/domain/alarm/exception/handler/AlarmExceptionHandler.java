package com.service.releasenote.domain.alarm.exception.handler;

import com.service.releasenote.domain.alarm.exception.AlarmNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AlarmExceptionHandler {

    @ExceptionHandler(AlarmNotFoundException.class)
    protected final ResponseEntity<String> notFoundAlarm(AlarmNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
