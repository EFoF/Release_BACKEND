package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id")Long company_id);

    @Query(value = "SELECT mp.member_id FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<Long> findMemberListByProjectId(@Param("project_id")Long project_id);

    @Query(value = "SELECT * FROM member_project mp WHERE mp.project_id=:project_id AND mp.member_id=:member_id", nativeQuery = true)
    MemberProject findByMemberAndProject(@Param("member_id") Long memberId, @Param("project_id") Long projectId);

    @Query(value = "SELECT * FROM member_project mp WHERE mp.member_id =:currentMemberId", nativeQuery = true)
    Optional<MemberProject> findByMemberId(Long currentMemberId);

    @Query(value = "SELECT * FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<MemberProject> findMemberProjectByProjectId(@Param("project_id")Long project_id);
}
