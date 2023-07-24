package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberCompany;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MemberCompanyRepository extends JpaRepository<MemberCompany, Long> {

//    @Query(value = "SELECT mc.member_id FROM member_company mc WHERE mc.company_id = :company_id", nativeQuery = true)
    List<Long> findMembersByCompanyId(@Param("company_id")Long company_id);

//    Optional<List<Company>> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"company"})
    List<MemberCompany> findByMemberId(Long memberId);

//    Optional<List<Long>> findCompanyIdByMember(Member member);
}
