package com.service.releasenote.domain.company.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberCompany;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.service.releasenote.domain.company.dto.CompanyDTO.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {

    private final MemberRepository memberRepository;

    private final CompanyRepository companyRepository;

    private final MemberCompanyRepository memberCompanyRepository;

    @Transactional
    public Long createCompany(CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);  // TODO: exception 추가 후 수정
        Company company = companyRepository.save(createCompanyRequestDTO.toEntity());
        MemberCompany memberCompany = MemberCompany.builder()
                .role(Role.OWNER)
                .company(company)
                .member(member)
                .build();
        memberCompanyRepository.save(memberCompany);
        return company.getId();
    }

}
