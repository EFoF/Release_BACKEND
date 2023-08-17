package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.MemberCompanyService;
import com.service.releasenote.domain.member.dto.MemberCompanyDTO;
import com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberRequestDTO;
import com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberResponseDTO;
import com.service.releasenote.domain.member.dto.MemberDTO.MemberListDTO;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"member_company"})
public class MemberCompanyController {
    private final MemberCompanyService memberCompanyService;

    @GetMapping(value = "/api/companies/{company_id}/members")
    public List<MemberListDTO> findMembersByCompanyId(@PathVariable Long company_id) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        List<MemberListDTO> memberList = memberCompanyService.findMembersByCompanyId(company_id, currentMemberId);

        return memberList;
    }

    @PostMapping(value = "/api/companies/{company_id}/members")
    public AddMemberResponseDTO addMemberCompany(@PathVariable Long company_id, @RequestBody AddMemberRequestDTO addMemberRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        AddMemberResponseDTO addMember = memberCompanyService.addMemberCompany(company_id, addMemberRequestDTO, currentMemberId);

        // TODO: 반환 데이터
        return addMember;
    }

    @DeleteMapping(value = "/api/companies/{company_id}/members")
    public Long deleteMemberCompany(@PathVariable Long company_id, @RequestBody DeleteMemberRequestDTO request) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long deleteMemberId = memberCompanyService.deleteMemberCompany(company_id, request.getEmail(), currentMemberId);

        // TODO: 반환 데이터
        return deleteMemberId;
    }
}
