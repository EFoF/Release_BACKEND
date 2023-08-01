package com.service.releasenote.domain.project.api;

import com.service.releasenote.domain.project.application.ProjectPaginationService;
import com.service.releasenote.domain.project.dto.ProjectPaginationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.service.releasenote.domain.project.dto.ProjectPaginationDto.*;

@RestController
@RequiredArgsConstructor
public class ProjectPaginationTestController {

    private final ProjectPaginationService projectPaginationService;

    @GetMapping("/test")
    public ProjectPaginationDtoWrapper paginationTest(Pageable pageable) {
        return projectPaginationService.getProjectPage(pageable);
    }
}
