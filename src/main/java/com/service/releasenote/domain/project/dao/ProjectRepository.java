package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id")Long company_id);

    @Query(value = "SELECT mp.role FROM member_project mp WHERE mp.member_id = :member_id and mp.project_id = :project_id", nativeQuery = true)
    Role findRoleByMemberIdAndProjectId(@Param("project_id")Long project_id, @Param("member_id")Long member_id);

//    @Query(value = "SELECT ")
//    List<Category> findCategoryByProjectId(@Param("project_id")Long project_id);

    @Query(value = "SELECT * FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<MemberProject> findMemberProjectByProjectId(@Param("project_id")Long project_id);


    Optional<List<Project>> findByCompany(Company company);

}
