package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id")Long company_id);

//    @Query(value = "SELECT mp.member_id FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<Long> findMembersByProjectId(@Param("project_id")Long project_id);

//    @Query(value = "SELECT * FROM member_project mp WHERE mp.project_id=:project_id AND mp.member_id=:member_id", nativeQuery = true)
    MemberProject findByMemberAndProject(@Param("member_id") Long memberId, @Param("project_id") Long projectId);

//    @Query(value = "SELECT * FROM member_project mp WHERE mp.member_id =:currentMemberId", nativeQuery = true)
    Optional<MemberProject> findMemberProjectByMemberId(Long currentMemberId);

    @Query(value = "SELECT * FROM member_project mp WHERE mp.project_id = :project_id", nativeQuery = true)
    List<MemberProject> findMemberProjectByProjectId(@Param("project_id")Long project_id);

    @EntityGraph(attributePaths = {"project"})
    Optional<MemberProject> findByMemberIdAndProjectId(Long memberId, Long projectId);

    @EntityGraph(attributePaths = {"project"})
    List<MemberProject> findByMemberId(Long currentMemberId);

    //    @Query(value = "SELECT mp.role FROM member_project mp WHERE mp.member_id = :member_id and mp.project_id = :project_id", nativeQuery = true)
    Role findRoleByMemberIdAndProjectId(@Param("project_id")Long project_id, @Param("member_id")Long member_id);

    Member findByMember(Long currentMemberId);

    List<MemberProject> findByProjectId(Long projectId);
}
