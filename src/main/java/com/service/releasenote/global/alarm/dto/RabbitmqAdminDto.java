package com.service.releasenote.global.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RabbitmqAdminDto {

    @Getter
    @AllArgsConstructor
    public static class SaveQueueRequest {
        private String queueName;
        private String bindingKey;
    }

    @Getter
    @AllArgsConstructor
    public static class SaveExchangeRequest {
        private String name;
        private String type;
        private Boolean isAutoDeleted;
        private Boolean isDelayed;
        private Boolean isDurable;
        private Boolean isInternal;

    }
}
