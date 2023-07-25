package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberDTO.MemberListDTO;
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
import java.util.stream.Collectors;

import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCompanyService {

    private final MemberRepository memberRepository;

    private final CompanyRepository companyRepository;

    private final MemberCompanyRepository memberCompanyRepository;


    public List<MemberListDTO> findMembersByCompanyId(Long companyId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 로그인 되지 않은 경우
        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        List<MemberCompany> memberCompanyList = memberCompanyRepository.findByCompanyId(companyId);
        List<MemberListDTO> memberList = memberCompanyList.stream().map(memberCompany -> {
            Member member = memberCompany.getMember();
            MemberListDTO memberListDTO = new MemberListDTO();
            memberListDTO.setId(member.getId());
            memberListDTO.setName(member.getUserName());
            memberListDTO.setEmail(member.getEmail());
            return memberListDTO;
        }).collect(Collectors.toList());

        return memberList;
    }


    @Transactional
    public AddMemberResponseDTO addMemberCompany(Long company_id, AddMemberRequestDTO addMemberRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // TODO: exception
        List<Long> memberListByCompanyId = memberCompanyRepository.findMembersByCompanyId(company_id);
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
    // TODO: 삭제 예외처리 추가 (ROLE 관련)
    public Long deleteMemberCompany(Long company_id, String email) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // TODO: exception
        List<Long> memberListByCompanyId = memberCompanyRepository.findMembersByCompanyId(company_id);
        if(!memberListByCompanyId.contains(currentMemberId)) {
            throw new UserNotFoundException();
        }

        // TODO: exception
        Member member = memberRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        if(!memberListByCompanyId.contains(member.getId())) {
            throw new UserNotFoundException();
        }

        MemberCompany deleteMember = memberCompanyRepository.findByMemberAndCompany(company_id, member.getId());
        memberCompanyRepository.delete(deleteMember);

        return member.getId();
    }

}
