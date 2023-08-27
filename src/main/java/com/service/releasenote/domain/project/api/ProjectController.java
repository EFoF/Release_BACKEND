package com.service.releasenote.domain.project.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.release.api.ReleaseController;
import com.service.releasenote.global.log.CommonLog;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.service.releasenote.domain.project.dto.ProjectDto.*;
import com.service.releasenote.domain.company.dto.CompanyDTO.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Api(tags = {"project"})
public class ProjectController {
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    /**
     * 프로젝트 생성 Api
     * @param company_id
     * @return ResponseEntity<CreateProjectResponseDto>
     */
    @ApiOperation("API for project creation")
    @PostMapping("/api/companies/{company_id}/projects")
    public ResponseEntity<Long> createProject(
            @RequestBody CreateProjectRequestDto createProjectRequestDto,
            @PathVariable Long company_id) throws JsonProcessingException {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long project = projectService.createProject(createProjectRequestDto, company_id, currentMemberId);
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(createProjectRequestDto), "Post", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    /**
     * 특정 회사의 프로젝트 조회 Api
     * @param company_id
     * @return ResponseEntity<projectListByCompany>
     * */
    @ApiOperation("API for project inquiry of specific company")
    @GetMapping(value = "/api/companies/{company_id}/projects")
    public ResponseEntity<FindProjectListByCompanyIdResponseDto> projectList(@PathVariable Long company_id) throws JsonProcessingException {
        FindProjectListByCompanyIdResponseDto projectListByCompany = projectService.findProjectListByCompanyId(company_id);
        CommonLog commonLog = new CommonLog(projectListByCompany.getName() + " 회사의 프로젝트 리스트 조회", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
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
    @GetMapping("/api/companies/projects")
    public ProjectPaginationDtoWrapper myProjectList(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return projectService.getProjectPage(pageable, currentMemberId);
    }

    @GetMapping("/api/companies/{companyId}/myProjects")
    public FindProjectListByCompanyIdResponseDto myProjectListInCompany(@PathVariable(name = "companyId") Long companyId, Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return projectService.getMyProjectPageWithCompany(companyId, currentMemberId);
    }

    /**
     * 프로젝트 수정 Api
     * @param project_id
     * @return ResponseEntity
     * */
    @ApiOperation("API for project modification")
    @PutMapping(value = "/api/companies/projects/{project_id}")
    public ResponseEntity updateProject(
            @PathVariable Long project_id,
            @RequestBody UpdateProjectRequestDto updateProjectRequestDto) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        projectService.updateProject(updateProjectRequestDto, project_id, currentMemberId);
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(updateProjectRequestDto), "Put", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 프로젝트 삭제 Api
     * @param company_id
     * @param project_id
     * @return ResponseEntity
     * */
    @ApiOperation("API for deleting projects")
    @DeleteMapping(value = "/api/companies/{company_id}/projects/{project_id}")
    public ResponseEntity deleteProject(
            @PathVariable Long company_id, @PathVariable Long project_id) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        projectService.deleteProject(company_id, project_id, currentMemberId);
        CommonLog commonLog = new CommonLog(company_id + "번 회사의 " + project_id + " 번 프로젝트 삭제", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}