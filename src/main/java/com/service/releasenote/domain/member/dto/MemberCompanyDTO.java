package com.service.releasenote.domain.member.dto;

import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberCompany;
import com.service.releasenote.domain.member.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberCompanyDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMemberRequestDTO {
        private String email;

        public MemberCompany toEntity(Company company, Member member) {
            return MemberCompany.builder()
                    .member(member)
                    .company(company)
                    .role(Role.MEMBER)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMemberResponseDTO {
        private Long member_id;
        private Long company_id;
        private Role role;

        public AddMemberResponseDTO toResponseDTO(MemberCompany memberCompany) {
            return AddMemberResponseDTO.builder()
                    .member_id(memberCompany.getMember().getId())
                    .company_id(memberCompany.getCompany().getId())
                    .role(memberCompany.getRole())
                    .build();
        }
    }

}
