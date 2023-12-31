package com.service.releasenote.domain.release.dto;

import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.domain.release.model.Tag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;

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
        private LocalDateTime releaseDate;
        private String lastModifierEmail;
        private String lastModifierName;
        private String content;
        private String version;
        private Tag tag;
        private Long id;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseInfoDto {
        List<ReleaseDtoEach> releaseDtoList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectReleasesDtoEach {
        CategoryResponseDto categoryResponseDto;
        List<ReleaseDtoEach> releaseDtoList;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectReleasesDto {
        List<ProjectReleasesDtoEach> projectReleasesDto;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseModifyRequestDto {
        private LocalDateTime releaseDate;
        private String version;
        private String message;
        private Tag tag;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseModifyResponseDto {
        private LocalDateTime lastModifiedTime;
        private LocalDateTime releaseDate;
        private String lastModifierName;
        private String version;
        private String message;
        private Tag tag;
    }

}