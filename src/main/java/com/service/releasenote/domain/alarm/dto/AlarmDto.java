package com.service.releasenote.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AlarmDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmInfoDtoEach {
        private Long id;
        private String message;
        private Long authorId;
        private String authorEmail;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmInfoDto {
        private List<AlarmInfoDtoEach> alarmInfoDtoList;
    }
}
