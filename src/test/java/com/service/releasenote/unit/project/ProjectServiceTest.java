package com.service.releasenote.unit.project;

import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.dto.ProjectDto.*;
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
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
    ProjectService projectService;

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
                .userName("test_user_name")
                .email("test_email@test.com")
                .password(passwordEncoder.encode("test_password"))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .isDeleted(false)
                .build();
    }

    public CreateProjectRequestDto createProjectSaveRequest() {
        return CreateProjectRequestDto.builder()
                .description("test project description")
                .title("test project title")
                .scope(true)
                .build();
    }

    public UpdateProjectRequestDto updateProjectRequest() {
        return UpdateProjectRequestDto.builder()
                .description("modified project description")
                .title("modified project title")
                .scope(false)
                .build();
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 생성 테스트")
    public void saveProjectForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(currentMemberId);
        CreateProjectRequestDto projectSaveRequest = createProjectSaveRequest();

        //when
        when(companyRepository.findById(company.getId())).thenReturn(Optional.ofNullable(company));
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(preparedMemberList);
        when(projectRepository.save(any())).thenReturn(project);

        //then
        Long projectId = projectService.createProject(projectSaveRequest, company.getId());
        assertThat(projectId).isEqualTo(project.getId());
    }

    @Test
    @DisplayName("실패 - 프로젝트 생성 테스트 - 인증되지 않은 사용자")
    public void saveProjectForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        CreateProjectRequestDto projectSaveRequest = createProjectSaveRequest();

        //when
        when(companyRepository.findById(company.getId())).thenReturn(Optional.ofNullable(company));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(projectRepository.save(any())).thenReturn(project);

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> projectService.createProject(projectSaveRequest, company.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 생성 테스트 - 존재하지 않는 회사")
    public void saveProjectForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        CreateProjectRequestDto projectSaveRequest = createProjectSaveRequest();

        //when
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(new ArrayList<>());
        when(companyRepository.findById(company.getId())).thenReturn(Optional.empty());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(projectRepository.save(any())).thenReturn(project);

        //then
        Assertions.assertThrows(CompanyNotFoundException.class,
                () -> projectService.createProject(projectSaveRequest, company.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 수정 테스트")
    public void updateProjectForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(currentMemberId);
        UpdateProjectRequestDto updateProjectRequestDto = updateProjectRequest();

        //when
        when(memberRepository.findById(currentMemberId)).thenReturn(Optional.ofNullable(member));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(preparedMemberList);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.ofNullable(company));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));

        //then
        UpdateProjectResponseDto updateProjectResponseDto = projectService.updateProject(updateProjectRequestDto, project.getId());
        assertThat(updateProjectResponseDto.getDescription()).isEqualTo("modified project description");
        assertThat(updateProjectResponseDto.getTitle()).isEqualTo("modified project title");
        assertThat(updateProjectResponseDto.isScope()).isEqualTo(false);
    }

    @Test
    @DisplayName("실패 - 프로젝트 수정 테스트 - 인증되지 않은 사용자")
    public void updateProjectForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        UpdateProjectRequestDto updateProjectRequestDto = updateProjectRequest();

        //when
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(new ArrayList<>());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> projectService.updateProject(updateProjectRequestDto, project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 수정 테스트 - 프로젝트에 속하지 않은 사용자")
    public void updateProjectForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(1L);
        UpdateProjectRequestDto updateProjectRequestDto = updateProjectRequest();

        //when
        when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(member));
        when(memberProjectRepository.findMemberIdByProjectId(any())).thenReturn(new ArrayList<>());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> projectService.updateProject(updateProjectRequestDto, project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 수정 테스트 - 존재하지 않는 프로젝트")
    public void updateProjectForFailureByNotExistProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(currentMemberId);
        UpdateProjectRequestDto updateProjectRequestDto = updateProjectRequest();

        //when
        when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(member));
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateProject(updateProjectRequestDto, project.getId()));
    }

    @Test
    @DisplayName("실패 - 카테고리 삭제 테스트 - 인증되지 않은 사용자")
    public void deleteProjectForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);

        //when & then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> projectService.deleteProject(company.getId(), project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 삭제 테스트 - 프로젝트에 속하지 않은 사용자")
    public void deleteProjectForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> projectService.deleteProject(company.getId(), project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 프로젝트 삭제 테스트 - 존재하지 않는 프로젝트")
    public void deleteProjectForFailureByNotExistsProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);

        //when
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> projectService.deleteProject(company.getId(), project.getId()));

    }
}