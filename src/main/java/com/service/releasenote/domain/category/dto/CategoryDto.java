package com.service.releasenote.domain.category.dto;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategoryDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveCategoryRequest {
         private String title;
         private String description;

        public Category toEntity(Project project) {
            return Category.builder()
                    .title(this.title)
                    .description(this.description)
                    .project(project)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEachDto {
        private String title;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfoDto {
        List<CategoryEachDto> categoryEachDtoList;
    }
}
