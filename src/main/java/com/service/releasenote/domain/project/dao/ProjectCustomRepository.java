package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProjectCustomRepository {

    Page<ProjectDto.ProjectPaginationDtoEach> paginationTest(Long companyId, Pageable pageable);
}
