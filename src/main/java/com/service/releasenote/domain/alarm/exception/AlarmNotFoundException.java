package com.service.releasenote.domain.alarm.exception;

public class AlarmNotFoundException extends RuntimeException{
    public AlarmNotFoundException() {
        super("존재하지 않는 알람입니다.");
    }

    public AlarmNotFoundException(String message) {
        super(message);
    }
}
