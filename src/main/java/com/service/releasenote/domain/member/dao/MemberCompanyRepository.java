package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberCompany;
import com.service.releasenote.domain.member.model.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MemberCompanyRepository extends JpaRepository<MemberCompany, Long> {

//    @Query(value = "SELECT mc.member_id FROM member_company mc WHERE mc.company_id = :company_id", nativeQuery = true)
    List<Long> findMembersByCompanyId(@Param("company_id")Long company_id);

    @Query(value = "SELECT mc.member_id FROM member_company mc WHERE mc.company_id = :company_id", nativeQuery = true)
    List<Long> findMemberListByCompanyId(@Param("company_id")Long company_id);

//    Optional<List<Company>> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"company"})
    List<MemberCompany> findByMemberId(Long memberId);

    @Query(value = "SELECT mc.role FROM member_company mc WHERE mc.member_id = :member_id and mc.company_id = :company_id", nativeQuery=true)
    Optional<Role> findRoleByMemberIdAndCompanyId(@Param("company_id") Long company_id, @Param("member_id") Long member_id);

    @Query(value = "SELECT * FROM member_company mc WHERE mc.company_id = :company_id", nativeQuery = true)
    List<MemberCompany> findMemberCompanyByCompanyId(Long company_id);

    Optional<MemberCompany> findByMemberIdAndCompanyId(Long currentMemberId, Long id);

//    Optional<List<Long>> findCompanyIdByMember(Member member);
}
