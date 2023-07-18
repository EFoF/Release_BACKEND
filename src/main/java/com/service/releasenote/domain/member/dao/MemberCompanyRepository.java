package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberCompany;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< Updated upstream

public interface MemberCompanyRepository extends JpaRepository<MemberCompany, Long> {
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberCompanyRepository extends JpaRepository<MemberCompany, Long> {

    @Query(value = "SELECT mc.member_id FROM member_company mc WHERE mc.company_id = :company_id", nativeQuery = true)
    List<Long> findMemberListByCompanyId(@Param("company_id")Long company_id);
>>>>>>> Stashed changes
}
