package com.service.releasenote.domain.company.dto;

import com.service.releasenote.domain.company.model.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CompanyDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCompanyRequestDTO {
        private String name;
        private String imageUrl;
        public Company toEntity() {
            return Company.builder()
                    .name(this.name)
                    .ImageURL(this.imageUrl)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyProjectListByCompanyDto {

    }
}
