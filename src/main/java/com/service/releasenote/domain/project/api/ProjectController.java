package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.project.application.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(code=201, message="생성 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 회사"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "요청 성공"),
            @ApiResponse(code = 404, message = "존재하지 않는 회")
    })
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "요청 성공"),
            @ApiResponse(code = 404, message = "존재하지 않는 회사")
    })
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
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 프로젝트"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    @DeleteMapping(value = "/companies/{company_id}/projects/{project_id}")
    public ResponseEntity deleteProject(
            @PathVariable Long company_id, @PathVariable Long project_id) {
        // 서비스 로직에서 프로젝트 삭제
        projectService.deleteProject(company_id, project_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
