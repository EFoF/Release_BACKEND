package com.service.releasenote.domain.member.dto;

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

import java.time.LocalDateTime;
import java.util.Optional;

public class MemberProjectDTO {
    @Getter
    @AllArgsConstructor
//    @NoArgsConstructor    // 왜 오류나지?
    public static class RoleDto {
        // 프로젝트 생성시 member_project 데이터 추가 dto
//        private Role role;

        public MemberProject toEntity(Member member, Project project, Role role) {
            return MemberProject.builder()
                    .role(role)
                    .member(member)
                    .project(project)
                    .build();
        }
    }

    @Getter
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

        public MemberProjectDTO.AddProjectMemberResponseDto toResponseDto(MemberProject memberProject) {
            return AddProjectMemberResponseDto.builder()
                    .member_id(memberProject.getMember().getId())
                    .project_id(memberProject.getProject().getId())
                    .role(memberProject.getRole())
                    .build();
        }
    }


}
