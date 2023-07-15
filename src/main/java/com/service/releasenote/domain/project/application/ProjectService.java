package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.DuplicatedProjectTitleException;
import com.service.releasenote.domain.project.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public ProjectDto.CreateProjectResponseDto createProject
            (ProjectDto.CreateProjectRequestDto createProjectRequestDto, Long company_id) {

        // 회사가 존재하지 않는 경우 예외 처리
        Company company = companyRepository.findById(company_id)
                .orElseThrow(CompanyNotFoundException::new);

        Project newProject = createProjectRequestDto.toEntity(company);

        // 해당 회사 프로젝트들 중 프로젝트의 이름이 이미 존재하는 경우 예외 처리
        List<String> TitleByCompanyId = projectRepository.findTitleByCompanyId(company_id);
        for (String s : TitleByCompanyId) {
            if(newProject.getTitle().equals(s)){
                throw new DuplicatedProjectTitleException();
            }
        }

        Project saveProject = projectRepository.save(newProject);

        return new ProjectDto.CreateProjectResponseDto().toResponseDto(saveProject);
    }
}
