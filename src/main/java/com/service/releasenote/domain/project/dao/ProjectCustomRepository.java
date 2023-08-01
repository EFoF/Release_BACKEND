package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.project.dto.ProjectPaginationDto;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.service.releasenote.domain.project.dto.ProjectPaginationDto.*;

public interface ProjectCustomRepository {

    Page<ProjectPaginationDtoEach> paginationTest(Long companyId, Pageable pageable);
}
