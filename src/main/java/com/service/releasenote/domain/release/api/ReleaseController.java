package com.service.releasenote.domain.release.api;

import com.service.releasenote.domain.release.application.ReleaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.release.dto.ReleaseDto.ReleaseInfoDto;
import static com.service.releasenote.domain.release.dto.ReleaseDto.SaveReleaseRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"release"})
public class ReleaseController {

    private final ReleaseService releaseService;

    @ApiOperation(value="api for save release")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/companies/projects/{projectId}/categories/{categoryId}/releases")
    public Long saveRelease(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestBody SaveReleaseRequest saveReleaseRequest
    ) {
        return releaseService.saveRelease(saveReleaseRequest, projectId, categoryId);
    }

    @GetMapping("/companies/projects/categories/{categoryId}/releases")
    public ReleaseInfoDto getReleaseByProject(
            @PathVariable(name = "categoryId") Long categoryId
    ) {
        return releaseService.findReleasesByCategoryId(categoryId);
    }
}