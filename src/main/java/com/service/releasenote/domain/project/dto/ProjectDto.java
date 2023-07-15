package com.service.releasenote.domain.project.dto;


import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProjectRequestDto {
        // 프로젝트 생성 요청 dto
        private String title;
        private String description;
        private boolean scope;

        public Project toEntity(Company company) {
            return Project.builder()
                    .company(company)
                    .title(this.getTitle())
                    .description(this.getDescription())
                    .scope(this.isScope())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProjectResponseDto {
        // 프로젝트 생성 응답 dto
        private String title;
        private String description;
        private boolean scope;
        private LocalDateTime create_date;
//        LocalDateTime modified_date;    // 생성이라 필요없을 듯

        public CreateProjectResponseDto toResponseDto(Project project) {
            return CreateProjectResponseDto.builder()
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .scope(project.isScope())
                    .create_date(project.getCreateDate())
                    .build();
        }
    }

    public static class FindMyProjectResponseDto {
    }
}
