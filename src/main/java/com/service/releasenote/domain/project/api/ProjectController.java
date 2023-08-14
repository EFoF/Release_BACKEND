package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.global.util.SecurityUtil;
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
    public ResponseEntity<Long> createProject(
            @RequestBody CreateProjectRequestDto createProjectRequestDto,
            @PathVariable Long company_id) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long project = projectService.createProject(createProjectRequestDto, company_id, currentMemberId);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    /**
     * 특정 회사의 프로젝트 조회 Api
     * @param company_id
     * @return ResponseEntity<projectListByCompany>
     * */
    @ApiOperation("API for project inquiry of specific company")
    @GetMapping(value = "/companies/{company_id}/projects")
    public ResponseEntity<FindProjectListByCompanyIdResponseDto> projectList(@PathVariable Long company_id) {
        FindProjectListByCompanyIdResponseDto projectListByCompany = projectService.findProjectListByCompanyId(company_id);
        return new ResponseEntity<>(projectListByCompany, HttpStatus.OK);
    }
//    public ResponseEntity<FindProjectListByCompanyResponseDto> projectListByCompany(@PathVariable Long company_id, Pageable pageable) {
//        Long currentMemberId = SecurityUtil.getCurrentMemberId();
//        FindProjectListByCompanyResponseDto projectListByCompany = projectService.findProjectListByCompany(company_id, pageable, currentMemberId);
//        return new ResponseEntity<>(projectListByCompany, HttpStatus.OK);
//    }

    /**
     * 내가 속한 프로젝트 조회 Api
     * */
    @GetMapping("/companies/projects")
    public ProjectPaginationDtoWrapper paginationTest(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return projectService.getProjectPage(pageable, currentMemberId);
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
            @RequestBody UpdateProjectRequestDto updateProjectRequestDto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        projectService.updateProject(updateProjectRequestDto, project_id, currentMemberId);
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
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        projectService.deleteProject(company_id, project_id, currentMemberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}