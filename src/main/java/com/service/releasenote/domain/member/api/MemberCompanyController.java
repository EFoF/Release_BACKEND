package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.MemberCompanyService;
import com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberRequestDTO;
import com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberResponseDTO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"member_company"})
public class MemberCompanyController {
    private final MemberCompanyService memberCompanyService;

    @PostMapping(value = "/companies/{company_id}/members")
    public AddMemberResponseDTO addMemberCompany(@PathVariable Long company_id, @RequestBody AddMemberRequestDTO addMemberRequestDTO) {
        AddMemberResponseDTO addMember = memberCompanyService.addMemberCompany(company_id, addMemberRequestDTO);

        // TODO: 반환 데이터
        return addMember;
    }

    @DeleteMapping(value = "/companies/{company_id}/members")
    public Long deleteMemberCompnay(@PathVariable Long company_id, @RequestBody String email) {
        Long deleteMemberId = memberCompanyService.deleteMemberCompany(company_id, email);

        // TODO: 반환 데이터
        return deleteMemberId;
    }
}
