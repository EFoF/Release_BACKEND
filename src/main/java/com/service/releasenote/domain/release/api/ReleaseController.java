package com.service.releasenote.domain.release.api;

import com.service.releasenote.domain.release.application.ReleaseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.release.dto.ReleaseDto.*;

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
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("api for save release")
    @ApiResponses({
            @ApiResponse(code=201, message="생성 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리 또는 존재하지 않는 프로젝트"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
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
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true,
            dataType = "Boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({
            @ApiResponse(code=200, message="요청 성공"),
    })
    public ReleaseInfoDto releaseListByCategory(
            @PathVariable(name = "category_id") Long categoryId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) {
        return releaseService.findReleasesByCategoryId(categoryId, isDeveloper);
    }

    /**
     * 프로젝트로 릴리즈 조회
     * @param projectId
     * @return ProjectReleasesDto
     */
    @ApiOperation("api for get releases by project")
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true,
            dataType = "boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({
            @ApiResponse(code=201, message="생성 성공"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리 또는 존재하지 않는 프로젝트"),
    })
    @GetMapping("/companies/projects/{project_id}/categories/releases")
    public ProjectReleasesDto releaseListByProject(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) {
        return releaseService.findReleasesByProjectId(projectId, isDeveloper);
    }

    /**
     * 릴리즈 수정
     * @param projectId
     * @param categoryId
     * @param releaseId
     * @param releaseModifyRequestDto
     * @return ReleaseModifyResponseDto
     */
    @ApiOperation("api for modify releases")
    @PutMapping("/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}")
    @ApiResponses({
            @ApiResponse(code=200, message="요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리 또는 카테고리에 속하는 릴리즈가 존재하지 않음"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    public ReleaseModifyResponseDto releaseModify(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId,
            @PathVariable(name = "release_id") Long releaseId,
            @RequestBody ReleaseModifyRequestDto releaseModifyRequestDto
    ) {
        releaseService.modifyReleases(releaseModifyRequestDto, projectId, categoryId, releaseId);
        return releaseService.findReleaseAndConvert(releaseId);
    }

    /**
     * 릴리즈 삭제
     * @param projectId
     * @param categoryId
     * @param releaseId
     * @return String
     */
    @ApiOperation("api for delete releases")
    @DeleteMapping("/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}")
    @ApiResponses({
            @ApiResponse(code=200, message="요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리 또는 카테고리에 속하는 릴리즈가 존재하지 않음"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    public String ReleaseDelete(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId,
            @PathVariable(name = "release_id") Long releaseId
    ) {
        return releaseService.deleteRelease(projectId, categoryId, releaseId);
    }
}