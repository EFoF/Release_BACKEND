package com.service.releasenote.domain.project.dto;


import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {
    @Getter
    @Builder
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
        private Company company;

        public CreateProjectResponseDto toResponseDto(Project project, Company company) {
            return CreateProjectResponseDto.builder()
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .scope(project.isScope())
                    .create_date(project.getCreateDate())
                    .company(company)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindProjectListResponseDto {
        // 프로젝트 생성 응답 dto
        private Long project_id;
        private String title;
        private String description;
        private boolean scope;
        private LocalDateTime create_date;
        private LocalDateTime modified_date;

        public FindProjectListResponseDto toResponseDto(Project project) {
            return FindProjectListResponseDto.builder()
                    .project_id(project.getId())
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .scope(project.isScope())
                    .create_date(project.getCreateDate())
                    .modified_date(project.getModifiedDate())
                    .build();
        }
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProjectRequestDto {
        // 프로젝트 정보 수정 요청 dto
        private String title;
        private String description;
        private boolean scope;

        public Project toEntity(Project project) {
            return Project.builder()
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .scope(project.isScope())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProjectResponseDto {
        // 프로젝트 정보 수정 응답 dto
        private Long id;
        private String title;
        private String description;
        private boolean scope;
        private LocalDateTime modified_date;
//        private Long last_modifier;

        public UpdateProjectResponseDto toResponseDto(Project project) {
            return UpdateProjectResponseDto.builder()
                    .id(project.getId())
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .scope(project.isScope())
                    .modified_date(project.getModifiedDate())
                    .build();
        }
    }


}
