package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.project.application.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.service.releasenote.domain.project.dto.ProjectDto.*;
import com.service.releasenote.domain.company.dto.CompanyDTO.*;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Api(tags = {"project"})
public class ProjectController {
    private final ProjectService projectService;

    /**
     * 프로젝트 생성 Api
     * @param company_id
     * @return ResponseEntity<CreateProjectResponseDto>
     */
    @ApiOperation("API for project creation")
    @PostMapping("/companies/{company_id}/projects")
    public ResponseEntity<CreateProjectResponseDto> createProject(
            @RequestBody CreateProjectRequestDto createProjectRequestDto,
            @PathVariable Long company_id) {

        CreateProjectResponseDto project = projectService.createProject(createProjectRequestDto, company_id);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    /**
     * 특정 회사의 프로젝트 조회 Api
     * @param company_id
     * @param pageable
     * @return ResponseEntity<FindProjectListByCompanyResponseDto>
     * */
    @ApiOperation("API for project inquiry of specific company")
    @GetMapping(value = "/companies/{company_id}/projects")
    public ResponseEntity<FindProjectListByCompanyResponseDto> projectListByCompany(@PathVariable Long company_id, Pageable pageable) {
        FindProjectListByCompanyResponseDto projectListByCompany = projectService.findProjectListByCompany(company_id, pageable);
        return new ResponseEntity<>(projectListByCompany, HttpStatus.OK);
    }

    /**
     * 프로젝트 수정 Api
     * @param project_id
     * @return ResponseEntity
     * */
    @ApiOperation("API for project modification")
    @PutMapping(value = "/companies/projects/{project_id}")
    public ResponseEntity updateProject(
            @PathVariable Long project_id,
            @RequestBody UpdateProjectRequestDto updateProjectRequestDto
    ) {
        projectService.updateProject(updateProjectRequestDto, project_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 프로젝트 삭제 Api
     * @param company_id
     * @param project_id
     * @return ResponseEntity
     * */
    @ApiOperation("API for deleting projects")
    @DeleteMapping(value = "/companies/{company_id}/projects/{project_id}")
    public ResponseEntity deleteProject(
            @PathVariable Long company_id, @PathVariable Long project_id) {
        // 서비스 로직에서 프로젝트 삭제
        projectService.deleteProject(company_id, project_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
