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
    @PostMapping("/company/{companyId}/project/{projectId}/category/{categoryId}/release")
    public void saveRelease(
            @PathVariable(name = "companyId") Long companyId,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestBody SaveReleaseRequest saveReleaseRequest
    ) {
        releaseService.saveRelease(saveReleaseRequest);
    }

    @GetMapping("/company/{companyId}/project/{projectId}/category/{categoryId}/release")
    public ReleaseInfoDto getReleaseByProject(
            @PathVariable(name = "companyId") Long companyId,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "categoryId") Long categoryId
    ) {
        return releaseService.findReleasesByCategoryId(categoryId);
    }
}