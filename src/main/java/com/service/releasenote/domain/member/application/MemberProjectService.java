package com.service.releasenote.domain.member.application;
import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.alarm.model.Alarm;
import com.service.releasenote.domain.alarm.model.AlarmDomain;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberDTO;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProjectService {

    private final MemberProjectRepository memberProjectRepository;
    private final MemberCompanyRepository memberCompanyRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    @Transactional
    public AddProjectMemberResponseDto addProjectMember
            (AddProjectMemberRequestDto addProjectMemberRequestDto, Long projectId, Long currentMemberId) {

        // 프로젝트가 없으면 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // currentMemberId가 초대할 권한이 없으면 예외 처리 (프로젝트에 속해 있는지)
        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }

        // addProjectMemberRequestDto에서 email로 member 객체 생성
        String email = addProjectMemberRequestDto.getEmail();
        Optional<Member> memberByEmail = memberRepository.findByEmail(email);

        // memberId가 존재하지 않으면 예외 처리
        Member member = memberByEmail.orElseThrow(UserNotFoundException::new);

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
        alarmService.produceMessage(projectId, member.getId(), member.getUserName() + " 님을 초대하셨습니다.", AlarmDomain.MEMBER, currentMemberId);
        return new AddProjectMemberResponseDto().toResponseDto(saveMemberProject);
    }

    @Transactional
    public void deleteProjectMember(Long projectId, String memberEmail, Long currentMemberId) {

        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);

        // projectId가 존재하지 않으면 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // currentMemberId가 프로젝트의 멤버가 아니면 예외 처리
        MemberProject currentMemberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(ProjectPermissionDeniedException::new);

        // memberEmail을 가진 member의 id가 memberproject에 없으면 예외 처리
        Optional<Member> MemberByEmail = memberRepository.findByEmail(memberEmail);
        Member member = MemberByEmail.orElseThrow(UserNotFoundException::new);

        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!memberListByProjectId.contains(member.getId())){
            throw new UserNotFoundException();
        }

        // member를 member_project에서 삭제
        MemberProject deletedMember = memberProjectRepository.findByMemberIdAndProjectId(member.getId(), projectId)
                        .orElseThrow(UserNotFoundException::new);
        List<Alarm> alarmList = alarmRepository.findByMemberProjectId(deletedMember.getId());
        for (Alarm alarm : alarmList) {
            alarmRepository.delete(alarm);
        }
        memberProjectRepository.delete(deletedMember);
        alarmService.produceMessage(projectId, member.getId(), member.getUserName() + " 님을 추방하셨습니다.", AlarmDomain.MEMBER, currentMemberId);
    }

    public FindMemberListByProjectId findProjectMemberList(Long projectId) {
        List<Member> memberList = memberRepository.findByProjectId(projectId);

        List<MemberDTO.MemberListDTO> collect = memberList.stream()
                .map(m -> new MemberDTO.MemberListDTO().toResponseDto(m))
                .collect(Collectors.toList());

        return new FindMemberListByProjectId(collect);
    }
}
