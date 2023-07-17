package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCompanyRepository extends JpaRepository<MemberCompany, Long> {
}
