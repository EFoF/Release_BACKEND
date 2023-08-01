package com.service.releasenote.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProjectPaginationDto {

    @Getter
    @Builder
    @NoArgsConstructor
    public static class ProjectPaginationDtoEach {
        private String title;
        private String description;
        private Long id;
        private Long companyId;

        @QueryProjection
        public ProjectPaginationDtoEach (String title, String description, Long id, Long companyId){
            this.title = title;
            this.description = description;
            this.id = id;
            this.companyId = companyId;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectPaginationDtoWrapper {
        private Page<ProjectPaginationDtoEach> list;
    }
}
