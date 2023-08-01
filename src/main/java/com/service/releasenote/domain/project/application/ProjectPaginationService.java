package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.dto.ProjectPaginationDto;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.service.releasenote.domain.project.dto.ProjectPaginationDto.*;
import static com.service.releasenote.domain.project.dto.ProjectPaginationDto.ProjectPaginationDtoWrapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectPaginationService {

    private final ProjectRepository projectRepository;

    public ProjectPaginationDtoWrapper getProjectPage(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Page<ProjectPaginationDtoEach> projects = projectRepository.paginationTest(currentMemberId, pageable);
        return ProjectPaginationDtoWrapper.builder().list(projects).build();
    }

}
