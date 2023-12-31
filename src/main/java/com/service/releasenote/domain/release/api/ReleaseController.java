package com.service.releasenote.domain.release.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.member.api.AuthController;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.global.log.CommonLog;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.service.releasenote.domain.release.dto.ReleaseDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"release"})
public class ReleaseController {

    private final ReleaseService releaseService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);

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
    @PostMapping("/api/companies/projects/{projectId}/categories/{categoryId}/releases")
    public ReleaseDtoEach releaseAdd(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestBody SaveReleaseRequest saveReleaseRequest
    ) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(saveReleaseRequest), "Post", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return releaseService.saveRelease(saveReleaseRequest, projectId, categoryId, currentMemberId);
    }

    /**
     * category로 release 조회 api
     * @param categoryId
     * @return ReleaseInfoDto
     */
    @ApiOperation("api for get releases by category")
    @GetMapping("/api/companies/projects/categories/{id}/releases")
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true, dataType = "Boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({ @ApiResponse(code=200, message="요청 성공")})
    public ReleaseInfoDto releaseListByCategory(
            @PathVariable(name = "id") Long categoryId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) throws JsonProcessingException{
        String msg;
        if(isDeveloper) {
            msg = "개발자 -" + categoryId + "번 카테고리로 릴리즈 조회";
        } else {
            msg = "일반인 - " + categoryId + "번 카테고리로 릴리즈 조회";
        }
        CommonLog commonLog = new CommonLog(msg, "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return releaseService.findReleasesByCategoryId(categoryId, isDeveloper);
    }

    /**
     * 프로젝트로 릴리즈 조회
     * @param projectId
     * @return ProjectReleasesDto
     */
    @ApiOperation("api for get releases by project")
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true, dataType = "boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({ @ApiResponse(code=201, message="생성 성공"), @ApiResponse(code=404, message = "존재하지 않는 카테고리 또는 존재하지 않는 프로젝트"),})
    @GetMapping("/api/companies/projects/{project_id}/categories/releases")
    public ProjectReleasesDto releaseListByProject(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) throws JsonProcessingException{
        String msg;
        if(isDeveloper) {
            msg = "개발자 -" + projectId + "번 프로젝트로 릴리즈 조회";
        } else {
            msg = "일반인 - " + projectId + "번 프로젝트로 릴리즈 조회";
        }
        CommonLog commonLog = new CommonLog(msg, "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
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
    @PutMapping("/api/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}")
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
    ) throws JsonProcessingException{
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        releaseService.modifyReleases(releaseModifyRequestDto, projectId, categoryId, releaseId, currentMemberId);
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(releaseModifyRequestDto), "Put", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
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
    @DeleteMapping("/api/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}")
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
    ) throws JsonProcessingException{
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        CommonLog commonLog = new CommonLog(releaseId + " 번 릴리즈 삭제", "Delete", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return releaseService.deleteRelease(projectId, categoryId, releaseId, currentMemberId);
    }
}