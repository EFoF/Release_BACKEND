package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.service.releasenote.domain.project.dto.ProjectDto.*;

@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     */
    @PostMapping("/company/{company_id}/project")
    public ResponseEntity<CreateProjectResponseDto> createProject
    (@RequestBody CreateProjectRequestDto createProjectRequestDto,
     @PathVariable Long company_id) {

        CreateProjectResponseDto project = projectService.createProject(createProjectRequestDto, company_id);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    /**
     * 프로젝트 삭제
     * */
    @DeleteMapping(value = "/company/{company_id}/project/{project_id}")
    public ResponseEntity deleteProject(
            @PathVariable Long company_id, @PathVariable Long project_id) {

        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 서비스 로직에서 프로젝트 삭제
        projectService.deleteProject(company_id, project_id, currentMemberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
