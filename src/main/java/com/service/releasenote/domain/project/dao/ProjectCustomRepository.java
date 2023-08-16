package com.service.releasenote.domain.project.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.service.releasenote.domain.project.dto.ProjectDto.*;


public interface ProjectCustomRepository {

    Page<ProjectPaginationDtoEach> findMyProjects(Long memberId, Pageable pageable);
    Page<ProjectPaginationDtoEach> findMyProjectsInCompany(Long companyId, Long memberId,Pageable pageable);
}
