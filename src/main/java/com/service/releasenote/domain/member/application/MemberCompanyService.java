package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberCompany;
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCompanyService {

    private final MemberRepository memberRepository;

    private final CompanyRepository companyRepository;

    private final MemberCompanyRepository memberCompanyRepository;

    @Transactional
    public AddMemberResponseDTO addMemberCompany(Long company_id, AddMemberRequestDTO addMemberRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // TODO: exception
        List<Long> memberListByCompanyId = memberCompanyRepository.findMemberListByCompanyId(company_id);
        if(!memberListByCompanyId.contains(currentMemberId)) {
            throw new UserNotFoundException();
        }

        // TODO: exception
        Member member = memberRepository.findByEmail(addMemberRequestDTO.getEmail()).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        if(memberListByCompanyId.contains(member.getId())) {
            throw new UserNotFoundException();
        }

        MemberCompany newMember = addMemberRequestDTO.toEntity(company, member);
        MemberCompany savedMember = memberCompanyRepository.save(newMember);

        return new AddMemberResponseDTO().toResponseDTO(savedMember);
    }

    @Transactional
    public Long deleteMemberCompany(Long company_id, String email) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // TODO: exception
        List<Long> memberListByCompanyId = memberCompanyRepository.findMemberListByCompanyId(company_id);
        if(!memberListByCompanyId.contains(currentMemberId)) {
            throw new UserNotFoundException();
        }

        // TODO: exception
        Member member = memberRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        if(memberListByCompanyId.contains(member.getId())) {
            throw new UserNotFoundException();
        }

        MemberCompany deleteMember = memberCompanyRepository.findByMemberAndCompany(company_id, member.getId());
        memberCompanyRepository.delete(deleteMember);

        return member.getId();
    }

}
