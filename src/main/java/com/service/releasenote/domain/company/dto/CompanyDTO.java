package com.service.releasenote.domain.company.dto;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.dto.ProjectDto;
import lombok.*;
import org.springframework.data.domain.Slice;


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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindProjectListByCompanyResponseDto {
        private Long companyId;
        private String name;
        private String imgURL;
        private Slice<ProjectDto.FindProjectListResponseDto> findProjectListResponseDtos;

        public FindProjectListByCompanyResponseDto toResponseDto(Company company, Slice<ProjectDto.FindProjectListResponseDto> findProjectListResponseDtos) {
            return FindProjectListByCompanyResponseDto.builder()
                    .companyId(company.getId())
                    .name(company.getName())
                    .imgURL(company.getImageURL())
                    .findProjectListResponseDtos(findProjectListResponseDtos)
                    .build();
        }
    }
}