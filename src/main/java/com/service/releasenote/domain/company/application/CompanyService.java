package com.service.releasenote.domain.company.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.dao.CompanyRepositoryImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final CompanyRepositoryImpl companyRepositoryImpl;

    private final MemberCompanyRepository memberCompanyRepository;

    @Transactional
    public Long createCompany(CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 로그인 되지 않은 경우
        // TODO: exception 추가 후 수정
        Member member = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        Company company = companyRepository.save(createCompanyRequestDTO.toEntity());
        MemberCompany memberCompany = MemberCompany.builder()
                .role(Role.OWNER)
                .company(company)
                .member(member)
                .build();
        memberCompanyRepository.save(memberCompany);
        return company.getId();
    }

    public Page<Company> findCompaniesByName(String name, Pageable pageable) {
        return companyRepositoryImpl.findCompaniesByName(name, pageable);
    }

}
