package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dto.ProjectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     */
    @PostMapping("/company/{company_id}/project")
    public ResponseEntity<ProjectDto.CreateProjectResponseDto> createProject
    (@Validated @RequestBody ProjectDto.CreateProjectRequestDto createProjectRequestDto,
     @PathVariable Long company_id) {

        ProjectDto.CreateProjectResponseDto project = projectService.createProject(createProjectRequestDto, company_id);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }
}
