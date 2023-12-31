package com.service.releasenote.domain.project.dao;

import com.service.releasenote.domain.project.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectCustomRepository{
    @Query(value = "SELECT p.title FROM project p WHERE p.company_id = :company_id", nativeQuery = true)
    List<String> findTitleByCompanyId(@Param("company_id") Long company_id);

    List<Project> findByCompanyId(Long companyId);


    @Query(value = "select p.* from member_project mp " +
            "join project p on p.project_id = mp.project_id " +
            "join company c on p.company_id = c.company_id " +
            "join member m on m.member_id = mp.member_id " +
            "where c.company_id = :company_id " +
            "and m.member_id = :member_id", nativeQuery = true)
    Slice<Project> findProjectsByCompanyIdAndMemberId(@Param("company_id") Long company_id, @Param("member_id") Long member_id, Pageable pageable);

}
