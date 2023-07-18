package com.service.releasenote.domain.company.dto;
import com.service.releasenote.domain.company.model.Company;
import lombok.*;

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
}
