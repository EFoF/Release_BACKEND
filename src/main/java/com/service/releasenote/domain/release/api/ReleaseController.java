package com.service.releasenote.domain.release.api;

import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dto.ReleaseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.release.dto.ReleaseDto.*;
import static com.service.releasenote.domain.release.dto.ReleaseDto.ReleaseInfoDto;
import static com.service.releasenote.domain.release.dto.ReleaseDto.SaveReleaseRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"release"})
public class ReleaseController {

    private final ReleaseService releaseService;

    /**
     * release 저장 api
     * @param projectId
     * @param categoryId
     * @param saveReleaseRequest
     * @return Long
     */
    @ApiOperation("api for save release")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/companies/projects/{projectId}/categories/{categoryId}/releases")
    public Long releaseAdd(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestBody SaveReleaseRequest saveReleaseRequest
    ) {
        return releaseService.saveRelease(saveReleaseRequest, projectId, categoryId);
    }

    /**
     * category로 release 조회 api
     * @param categoryId
     * @return ReleaseInfoDto
     */
    @ApiOperation("api for get releases by category")
    @GetMapping("/companies/projects/categories/{category_id}/releases")
    public ReleaseInfoDto releaseListByCategory(
            @PathVariable(name = "category_id") Long categoryId
    ) {
        return releaseService.findReleasesByCategoryId(categoryId);
    }

    /**
     * 프로젝트로 릴리즈 조회
     * @param projectId
     * @return ProjectReleasesDto
     */
    @ApiOperation("api for get releases by project")
    @GetMapping("/companies/projects/{project_id}/categories/releases")
    public ProjectReleasesDto releaseListByProject(
            @PathVariable(name = "project_id") Long projectId
    ) {
        return releaseService.findReleasesByProjectId(projectId);
    }
}