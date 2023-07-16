package com.service.releasenote.domain.release.dto;

import com.service.releasenote.domain.release.model.Tag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReleaseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveReleaseRequest {
        private Tag tag;
        private String title;
        private String content;


    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseDtoEach {
        private LocalDateTime lastModifiedTime;
        private String authorName;
        private String content;
        private String version;
        private Tag tag;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseInfoDto {
        List<ReleaseDtoEach> releaseDtoList;
    }

}