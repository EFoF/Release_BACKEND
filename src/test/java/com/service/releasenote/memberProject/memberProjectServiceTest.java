package com.service.releasenote.memberProject;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import com.service.releasenote.domain.member.error.exception.DuplicatedProjectMemberException;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class memberProjectServiceTest {
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    MemberProjectRepository memberProjectRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    CompanyRepository companyRepository;
    @MockBean
    MemberRepository memberRepository;
    @MockBean
    CategoryRepository categoryRepository;
    @Autowired
    MemberProjectService memberProjectService;

    public Company buildCompany(Long id) {
        return Company.builder()
                .ImageURL("test image url")
                .name("teset company name " + id)
                .id(id)
                .build();
    }

    public Project buildProject(Company company, Long id) {
        return Project.builder()
                .description("test project description " + id)
                .title("test project title " + id)
                .company(company)
                .scope(true)
                .id(id)
                .build();
    }

    public Member buildMember(Long id) { // Test 용 멤버 생성
        return Member.builder()
                .id(id)
                .userName("test_user_name " + id)
                .email("test_email@test.com")
                .password(passwordEncoder.encode("test_password"))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .isDeleted(false)
                .build();
    }

    public MemberProject buildMemberProject(Long id, Project project, Member member) {
        return MemberProject.builder()
                .id(id)
                .project(project)
                .member(member)
                .build();
    }

    public AddProjectMemberRequestDto SaveProjectMemberRequestDto() {
        return AddProjectMemberRequestDto.builder()
                .email("test_email@test.com")
                .build();
    }

    public MemberDTO.MemberListDTO getMemberEachDto(Long id) {
        return MemberDTO.MemberListDTO.builder()
                .id(id)
                .name("test_user_name " + id)
                .email("test_user_email " + id)
                .build();
    }

    public FindMemberListByProjectId getProjectMemberResponseDto(int number) {
        List<MemberDTO.MemberListDTO> list = new ArrayList<>();
        for(int i=1; i<=number; i++) {
            list.add(getMemberEachDto(Long.valueOf(i)));
        }
        return FindMemberListByProjectId.builder()
                .memberListDTOS(list)
                .build();
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 멤버 추가 테스트")
    public void saveProjectMemberForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);
        MemberProject memberProject1 = buildMemberProject(1L, project, member1);
        MemberProject memberProject2 = buildMemberProject(2L, project, member2);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(preparedMemberList);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(member2));
        when(memberProjectRepository.save(any())).thenReturn(memberProject2);

        //then
        AddProjectMemberResponseDto addProjectMemberResponseDto = memberProjectService.addProjectMember(addProjectMemberRequestDto, project.getId(), currentMemberId);
        assertThat(addProjectMemberResponseDto.getMember_id()).isEqualTo(2L);
        assertThat(addProjectMemberResponseDto.getProject_id()).isEqualTo(1L);
        assertThat(addProjectMemberResponseDto.getName()).isEqualTo("test_user_name 2");
    }

    @Test
    @DisplayName("실패 - 프로젝트 멤버 추가 테스트 - 인증되지 않은 사용자")
    public void saveProjectMemberForFailureByUnAuthorizedUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> memberProjectService.addProjectMember(addProjectMemberRequestDto, project.getId(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 멤버 추가 테스트 - 존재하지 않는 프로젝트")
    public void saveProjectMemberForFailureByNotExistProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> memberProjectService.addProjectMember(addProjectMemberRequestDto, project.getId(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 멤버 추가 테스트 - 권한이 없는 사용자")
    public void saveProjectMemberForFailureByPermissionDenied() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);
        MemberProject memberProject1 = buildMemberProject(1L, project, member1);
        MemberProject memberProject2 = buildMemberProject(2L, project, member2);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> memberProjectService.addProjectMember(addProjectMemberRequestDto, project.getId(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 멤버 추가 테스트 - 이미 초대된 사용자")
    public void saveProjectMemberForFailureByDuplicatedMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);
        preparedMemberList.add(invitedMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(preparedMemberList);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(member1));

        //then
        Assertions.assertThrows(DuplicatedProjectMemberException.class,
                () -> memberProjectService.addProjectMember(addProjectMemberRequestDto, project.getId(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 멤버 조회 테스트")
    public void getProjectMemberForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(2L);
        Member member3 = buildMember(3L);

        List<Member> memberList = new ArrayList<>();
        memberList.add(member1);
        memberList.add(member2);
        memberList.add(member3);

        //when
        memberRepository.findByProjectId(project.getId());
        when(memberRepository.findByProjectId(project.getId())).thenReturn(memberList);

        //then
        FindMemberListByProjectId findMemberListByProjectId = memberProjectService.findProjectMemberList(project.getId());
        assertThat(findMemberListByProjectId.getMemberListDTOS()).extracting("name")
                .contains("test_user_name 1", "test_user_name 2", "test_user_name 3");
    }

    @Test
    @DisplayName("실패 - 프로젝트 멤버 삭제 테스트 - 인증되지 않은 사용자")
    public void deleteProjectMemberForFailureByUnAuthorizedUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);

        //when & then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> memberProjectService.deleteProjectMember(project.getId(), member2.getEmail(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 멤버 삭제 테스트 - 존재하지 않는 프로젝트")
    public void deleteProjectMemberForFailureByNotExistProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> memberProjectService.deleteProjectMember(project.getId(), member2.getEmail(), currentMemberId));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 멤버 삭제 테스트 - 프로젝트에 존재하지 않는 사용자")
    public void deleteProjectMemberForFailureByNotExistUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        Long invitedMemberId = 2L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);
        preparedMemberList.add(invitedMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member1 = buildMember(currentMemberId);
        Member member2 = buildMember(invitedMemberId);
        MemberProject memberProject1 = buildMemberProject(1L, project, member1);

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member1));
        when(memberRepository.findById(invitedMemberId)).thenReturn(Optional.ofNullable(member2));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(preparedMemberList);
        when(memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, project.getId())).thenReturn(Optional.of(memberProject1));

        //then
        Assertions.assertThrows(UserNotFoundException.class,
                () -> memberProjectService.deleteProjectMember(project.getId(), member2.getEmail(), currentMemberId));
    }

}
