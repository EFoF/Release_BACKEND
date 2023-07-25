package com.service.releasenote.domain.member.application;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberProjectDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import com.service.releasenote.domain.member.error.exception.DeleteMemberPermissionDeniedException;
import com.service.releasenote.domain.member.error.exception.DuplicatedProjectMemberException;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.error.exception.UserPermissionDeniedException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
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
            (AddProjectMemberRequestDto addProjectMemberRequestDto, Long projectId) {

        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 프로젝트가 없으면 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // currentMemberId가 초대할 권한이 없으면 예외 처리 (프로젝트에 속해 있는지) (OWNER 아니어도 됨)
//        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
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
        List<Long> memberListByCompanyId = memberCompanyRepository.findMembersByCompanyId(company.getId());
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

    @Transactional
    public void deleteProjectMember(Long projectId, String memberEmail) {
        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);

        // projectId가 존재하지 않으면 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // currentMemberId가 삭제할 권한이 없으면(프로젝트의 OWNER이 아니면) 예외 처리
        // -> 변경 후: currentMemberId가 프로젝트의 멤버가 아니면 예외 처리
        MemberProject currentMemberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(UserNotFoundException::new);

//        if(!currentMember.getRole().equals(Role.OWNER)){
//            throw new DeleteMemberPermissionDeniedException();
//        }

        // memberEmail을 가진 member의 id가 memberproject에 없으면 예외 처리
        Optional<Member> MemberByEmail = memberRepository.findByEmail(memberEmail);
        Member member = MemberByEmail.orElseThrow(NullPointerException::new);

//        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!memberListByProjectId.contains(member.getId())){
            throw new UserNotFoundException();
        }

        // member를 member_project에서 삭제
        MemberProject deletedMember = memberProjectRepository.findByMemberIdAndProjectId(member.getId(), projectId)
                        .orElseThrow(UserNotFoundException::new);
        memberProjectRepository.delete(deletedMember);

    }
}
