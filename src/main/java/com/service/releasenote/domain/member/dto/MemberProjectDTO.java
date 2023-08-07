package com.service.releasenote.domain.member.dto;

import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.domain.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;


public class MemberProjectDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddProjectMemberRequestDto {
        // 프로젝트 멤버 추가 요청 dto
        private String email;

        public MemberProject toEntity(Project project, Member member) {
            return MemberProject.builder()
                    .role(Role.MEMBER)
                    .project(project)
                    .member(member)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddProjectMemberResponseDto {
        // 프로젝트 멤버 추가 응답 dto
        private Long member_id;
        private Long project_id;
        private Role role;
        private String name;

        public MemberProjectDTO.AddProjectMemberResponseDto toResponseDto(MemberProject memberProject) {
            return AddProjectMemberResponseDto.builder()
                    .member_id(memberProject.getMember().getId())
                    .project_id(memberProject.getProject().getId())
                    .role(memberProject.getRole())
                    .name(memberProject.getMember().getUserName())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindMemberListByProjectId {
        private List<MemberDTO.MemberListDTO> memberListDTOS;

        public FindMemberListByProjectId toResponseDto(List<MemberDTO.MemberListDTO> memberListDTOS) {
            return FindMemberListByProjectId.builder()
                    .memberListDTOS(memberListDTOS)
                    .build();
        }
    }

}
