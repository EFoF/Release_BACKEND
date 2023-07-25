package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id") Long company_id);

    Optional<Company> findCompanyById(Long projectId);

    List<Project> findByCompanyId(Long companyId);
}
