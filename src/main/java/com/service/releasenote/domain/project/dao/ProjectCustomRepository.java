package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.service.releasenote.domain.project.dto.ProjectDto.*;


public interface ProjectCustomRepository {

    Page<ProjectPaginationDtoEach> findMyProjects(Long memberId, Pageable pageable);

    List<MemberProject> findMyProjectsWithCompanyId(Long memberId, Long companyId);
}
