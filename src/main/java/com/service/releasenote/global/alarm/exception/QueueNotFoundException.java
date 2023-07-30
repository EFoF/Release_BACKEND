package com.service.releasenote.global.alarm.exception;

public class QueueNotFoundException extends RuntimeException {

    public QueueNotFoundException() {
        super("존재하지 않는 Queue입니다.");
    }

    public QueueNotFoundException(String message) {
        super(message);
    }
}
