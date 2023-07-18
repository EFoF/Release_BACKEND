package com.service.releasenote.domain.member.application;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberProjectDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import com.service.releasenote.domain.member.error.exception.DuplicatedProjectMemberException;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.error.exception.UserPermissionDeniedException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProjectService {

    private final MemberProjectRepository memberProjectRepository;
    private final ProjectRepository projectRepository;
    private final MemberCompanyRepository memberCompanyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AddProjectMemberResponseDto addProjectMember
            (AddProjectMemberRequestDto addProjectMemberRequestDto,
             Long projectId,
             Long currentMemberId) {

        // 프로젝트가 없으면 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // currentMemberId가 초대할 권한이 없으면 예외 처리 (프로젝트에 속해 있는지)
        List<Long> memberListByProjectId = memberProjectRepository.findMemberListByProjectId(projectId);
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }

        // addProjectMemberRequestDto에서 email로 member 객체 생성
        String email = addProjectMemberRequestDto.getEmail();
        Optional<Member> memberByEmail = memberRepository.findByEmail(email);

        // memberId가 존재하지 않으면 예외 처리
        Member member = memberByEmail.orElseThrow(NullPointerException::new);

        // member가 projectId의 회사에 속해있지 않으면 예외 처리
        Company company = project.getCompany();
        List<Long> memberListByCompanyId = memberCompanyRepository.findMemberListByCompanyId(company.getId());
        if(!memberListByCompanyId.contains(member.getId())) {
            throw new UserPermissionDeniedException();
        }

        // member가 이미 프로젝트의 멤버이면 예외 처리
        if(memberListByProjectId.contains(member.getId())) {
            throw new DuplicatedProjectMemberException();
        }

        // member_project에 저장 (role은 MEMBER)
        MemberProject newMemberProject = addProjectMemberRequestDto.toEntity(project, member);

        MemberProject saveMemberProject = memberProjectRepository.save(newMemberProject);

        return new AddProjectMemberResponseDto().toResponseDto(saveMemberProject);
    }
}
