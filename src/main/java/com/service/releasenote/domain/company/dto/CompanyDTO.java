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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyResponseDTO {
        private Long id;
        private String name;
        private String imageUrl;

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCompanyRequestDTO {
        private String name;
        private String imageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCompanyResponseDTO {
        private Long id;
        private String name;
        private String imageUrl;

        public UpdateCompanyResponseDTO toResponseDTO(Company company) {
            return UpdateCompanyResponseDTO.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .imageUrl(company.getImageURL())
                    .build();
        }
    }

}
