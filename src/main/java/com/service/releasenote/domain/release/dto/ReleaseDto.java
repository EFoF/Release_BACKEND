package com.service.releasenote.domain.release.dto;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.release.model.Releases;
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
        private String message;
        private String version;
        private LocalDateTime releaseDate;

        public Releases toEntity(Category category) {
            return Releases.builder()
                    .releaseDate(this.releaseDate)
                    .version(this.version)
                    .message(this.message)
                    .category(category)
                    .tag(this.tag)
                    .build();
        }

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseDtoEach {
        private LocalDateTime lastModifiedTime;
        private String lastModifierName;
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