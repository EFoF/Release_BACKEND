package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
