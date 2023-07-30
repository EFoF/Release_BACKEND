package com.service.releasenote.global.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RabbitmqDto {

    @Getter
    @AllArgsConstructor
    public static class SaveQueueRequest {
        private String queueName;
        // 또는 rountingKey. 일괄성 있게 bindingKey로 통일하겠음
        private String bindingKey;
    }
}
