package com.service.releasenote.domain.company.dto;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.dto.ProjectDto;
import lombok.*;

import java.util.List;

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

    //    ProjectDto.FindProjectListResponseDto
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindProjectListByCompanyResponseDto {
        private Long company_id;
        private String name;
        private String img_url;
        private List<ProjectDto.FindProjectListResponseDto> findProjectListResponseDtos;

        public FindProjectListByCompanyResponseDto toResponseDto(Company company, List<ProjectDto.FindProjectListResponseDto> findProjectListResponseDtos) {
            return FindProjectListByCompanyResponseDto.builder()
                    .company_id(company.getId())
                    .name(company.getName())
                    .img_url(company.getImageURL())
                    .findProjectListResponseDtos(findProjectListResponseDtos)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberCompanyListDTO {
        private Long id;
        private String name;
        private String imageUrl;
    }
}