package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id")Long company_id);

    @Query(value = "SELECT mp.member_id FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<Long> findMemberListByProjectId(@Param("project_id")Long project_id);
}
