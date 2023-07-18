package com.service.releasenote.domain.member.dto;

import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.model.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
