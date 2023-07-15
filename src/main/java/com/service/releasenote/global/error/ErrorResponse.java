package com.service.releasenote.global.error;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String exceptionName;
    private final String message;

    @Builder
    public ErrorResponse(String exceptionName, String message) {
        this.exceptionName = exceptionName;
        this.message = message;
    }
}
